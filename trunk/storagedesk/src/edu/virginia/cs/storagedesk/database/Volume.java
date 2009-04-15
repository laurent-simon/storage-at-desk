package edu.virginia.cs.storagedesk.database;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import edu.virginia.cs.storagedesk.common.Config;
import edu.virginia.cs.storagedesk.common.Util;

public class Volume implements Externalizable {
	private static Logger logger = Logger.getLogger(Volume.class);
	
	private static final long serialVersionUID = 0;
	
	private String name = Config.TARGET_NAME;
	
	private int id;
	private int numCopies;
	private long size;
	private int numChunks;
	private long chunkSize;
	
	private long numLUNs;	
	private long numBlocks;
	private long blockSize;
		
	private Mapping[][] mappings = new Mapping[1][1];
	
	public Volume() {
		
	}
	
	// calculates numChunks internally
	public Volume(String name, int copies, 
			long numLUNs, long numBlocks, long blockSize,
			long chunkSize) {
		this.name      = name;
		this.numCopies = copies;
		this.numLUNs   = numLUNs;
		this.numBlocks = numBlocks;
		this.blockSize = blockSize; 
		this.numChunks = (int) Math.ceil((numLUNs * blockSize * numBlocks)/ (double)chunkSize);
		this.chunkSize = chunkSize;
		this.size      = numLUNs * numBlocks * blockSize;
		this.mappings  = new Mapping[this.numCopies][this.numChunks];
		for (int i = 0; i < this.numCopies; i++) {
			for (int j = 0; j < this.numChunks; j++) {
				mappings[i][j] = new Mapping();
			}
		}
		logger.info("Volume " + this.id + " " + this.name + " " + 
				this.numLUNs + " " + this.size + ", " + 
				this.numChunks + " chunks of " + this.chunkSize + " bytes");
	}
	
	// old, should not use
	// numChunks should be calculated by Volume, not the caller of Volume
	public Volume(String name, int copies, 
				  long numLUNs, long numBlocks, long blockSize,
				  int numChunks, long chunkSize) {
		this.name      = name;
		this.numCopies = copies;
		this.numLUNs   = numLUNs;
		this.numBlocks = numBlocks;
		this.blockSize = blockSize; 
		this.numChunks = numChunks;
		this.chunkSize = chunkSize;
		this.size      = numLUNs * numBlocks * blockSize;
		this.mappings  = new Mapping[this.numCopies][this.numChunks];
		for (int i = 0; i < this.numCopies; i++) {
			for (int j = 0; j < this.numChunks; j++) {
				mappings[i][j] = new Mapping();
			}
	    }
		logger.info("Volume " + this.id + " " + this.name + " " + 
				    this.numLUNs + " " + this.size + ", " + 
				    this.numChunks + " chunks of " + this.chunkSize + " bytes");
	}
	
	public boolean assignMapping(Connection conn) {
		for (int replica = 0; replica < this.numCopies; replica++) {
		
			// Retrieve all available machines 
			Hashtable<String, Machine> machines = Machine.getAvailableMachines(conn, this.chunkSize);
			
			logger.info(machines.size() + " machines available");
			
			long totalChunks = 0;
			for (Enumeration<Machine> e = machines.elements() ; e.hasMoreElements() ;) {
		        Machine m = (Machine) e.nextElement();
		        totalChunks += m.getNumChunks();
		    }
			
			logger.info("Machine has " + totalChunks + " chunks vs. volume [" + replica + "] asks for " + this.numChunks + " chunks");
			
			if (totalChunks >= this.numChunks) {
				logger.info("Assign mappings to " + machines.size() + " machines");
				int nc = 0;
				for (Enumeration<Machine> e = machines.elements() ; e.hasMoreElements() ;) {
			        Machine m = (Machine) e.nextElement();
			        int availableChunks = m.getNumAvailableChunks(conn);
			        logger.debug("Machine (" + m.getId() + ") has " + availableChunks + " chunks available");
			        while (nc < this.numChunks && availableChunks > 0) {
			        	int chooseChunk = m.getAvailableChunk(conn);
			        	logger.debug("Assign chunk [" + nc + "] of volume [" + replica + "] to chunk [" +
			        				  chooseChunk + 
			        				  "] of Machine (" + m.getId() + ") ");
			        	insertMapping(conn, 
			        			 	  replica,
			        				  nc++,
				        		      m.getId(),
				        		      chooseChunk,
				        		      m.getIp());
			        	availableChunks--;
			        }
			        if (nc == this.numChunks) {
			        	break;
			        }
			    }				
			} else {
				logger.error("Cannot achieve mappings");
				return false;
			}
		}
		return true;
	}
	
	public boolean insertMapping(Connection conn, 
								 int replica,
								 int virtualChunkID,
			                     String machineID, 
			                     int physcialChunkID,
			                     String ip) {
		logger.debug("insert mapping(" + this.id + ", " +
				                         replica + ", " +
				                         virtualChunkID + "," +
				                         machineID + ", " +
				                         physcialChunkID + ", ");
		PreparedStatement stmt = null;
		try {
	        stmt = conn.prepareStatement("{ call sp_insertmapping(?, ?, ?, ?, ?) }");
	    
	        stmt.setLong(1, this.id);
	        stmt.setInt(2, replica);
	        stmt.setInt(3, virtualChunkID);
	        stmt.setString(4, machineID);
	        stmt.setInt(5, physcialChunkID);
	        
	        stmt.execute();
	        mappings[replica][virtualChunkID] = new Mapping(this.id, replica, virtualChunkID, 
	        													machineID, physcialChunkID, ip);
	        return true;	        	        
        }
        catch (Exception e) {
        	logger.error(Util.getStackTrace(e));
        	return false;
        } finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException sqlEx) { // ignore }
					stmt = null;
				}
			}
		}
	}
	
	public boolean existedMapping(Connection conn) {
		boolean mappingExisted = false;
		
		ResultSet rs = null;
		PreparedStatement stmt = null;
		try { 
	        stmt = conn.prepareStatement("{ call sp_mappingbyvolume(?) }");
	        stmt.setInt(1, this.id);
	        rs = stmt.executeQuery();
            
            while (rs.next()) {            	
            	int vcid = rs.getInt("virtualchunk");
            	int rid  = rs.getInt("replica");
            	String mid = rs.getString("machine");
            	int pcid = rs.getInt("physicalchunk");
            	String ip = rs.getString("ip");
            	            	
            	mappings[rid][vcid] = new Mapping (this.id, rid, vcid, mid, pcid, ip);
            	logger.debug("replica [" + rid + "] chunk [" + vcid + 
            			     "] ---> mid [" + mid + "] chunk [" + pcid + "]");
            	mappingExisted = true;
            
            } 
                        
            for (int i = 0; i < this.numCopies; i++) {
            	for (int j = 0; j < this.numChunks; j++) {
            		if (mappings[i][j] == null) {
            			logger.error("Volume [" + this.id + "] has wrong mapping for replica " + i + " chunk " + j);
            			return false;
            		}
            	}  
            }
        }
        catch (Exception e) {
        	logger.error(e.toString());
        } finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqlEx) { // ignore }
					rs = null;
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException sqlEx) { // ignore }
					stmt = null;
				}
			}
		}
        if (mappingExisted) {
        	logger.info("Mapping exists");
        } else {
        	logger.info("Mapping doesn't exist");
        }
        
        return mappingExisted;
	}
	
	public int inDatabase(Connection conn) {
		ResultSet rs = null;
		PreparedStatement stmt = null;
		try { 
			logger.trace("call sp_volumebyname('" + this.name +"')");
			
	        stmt = conn.prepareStatement("{ call sp_volumebyname(?) }");
	        stmt.setString(1, this.name);
	        rs = stmt.executeQuery();
	        
            if (rs.next()) {
            	this.id = rs.getInt("id");            	
            	logger.info("Volume exists in the DB (id " + this.id + ")");
                if (rs.next()) {
                	logger.error("Too many volumes with the same name " + this.name);
                }               
                return this.id;
            } else {
            	logger.info("No volume with name " + this.name);
            	return -1;
            }            
        }
        catch (Exception e) {
        	logger.error(e.toString());
        } finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqlEx) { // ignore }
					rs = null;
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException sqlEx) { // ignore }
					stmt = null;
				}
			}
		}
		return -1;
	}
	
	public int insert(Connection conn) {
		logger.info("To Insert Volume " + this.name);
		PreparedStatement stmt = null;
		try { 
	        stmt = conn.prepareStatement("{ call sp_insertvolume(?, ?, ?, ?) }");
	    
	        stmt.setString(1, this.name);
	        stmt.setInt(2, this.numCopies);
	        stmt.setInt(3, this.numChunks);
	        stmt.setLong(4, this.chunkSize);
	        
	        stmt.execute();
	        
            return inDatabase(conn);
        }
        catch (Exception e) {
        	logger.error(e.toString());
        } finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException sqlEx) { // ignore }
					stmt = null;
				}
			}
		}
        return -1;
	}
	
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.id        = in.readInt();
		this.numCopies = in.readInt();
		this.name      = in.readUTF();
		this.size      = in.readLong();
		this.numLUNs   = in.readLong();
		this.numBlocks = in.readLong();
		this.blockSize = in.readLong();
		this.numChunks = in.readInt();
		this.chunkSize = in.readLong();
		
		this.mappings  = new Mapping[this.numCopies][this.numChunks];
		
		logger.debug("Mapping " + this.numCopies + " copies " + this.numChunks + " chunks");
		
		for (int i = 0; i < this.numCopies; i++) {
			for (int j = 0; j < this.numChunks; j++) {
				mappings[i][j] = (Mapping) in.readObject();
			}
	    }
		
		logger.trace("Read Volume " + this.id + " " + this.name + " " + this.numLUNs + " " + this.size + ", " + 
			    this.numChunks + " chunks of " + this.chunkSize + " bytes");
	}
	
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(this.id);
		out.writeInt(this.numCopies);
		out.writeUTF(this.name);
		out.writeLong(this.size);
		out.writeLong(this.numLUNs);
		out.writeLong(this.numBlocks);
		out.writeLong(this.blockSize);
		out.writeInt(this.numChunks);
		out.writeLong(this.chunkSize);
		
		logger.debug("Mapping " + this.numCopies + " copies " + this.numChunks + " chunks");
		
		for (int i = 0; i < this.numCopies; i++) {
			for (int j = 0; j < this.numChunks; j++) {
				out.writeObject(mappings[i][j]);
			}
	    }
				
		logger.trace("Write Volume " + this.id + " " + this.name + " " + this.numLUNs + " " + this.size + ", " + 
			    this.numChunks + " chunks of " + this.chunkSize + " bytes");
	}
	
	public long getSize() {
		return this.size;
	}
	
	public long getBlockSize() {
		return blockSize;
	}
	
	public void setBlockSize(long blockSize) {
		this.blockSize = blockSize;
	}
	
	public int getId() {
		return id;
	}
	
	public long getNumblock() {
		return numBlocks;
	}
	
	public void setNumblock(long numblock) {
		this.numBlocks = numblock;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getNumLuns() {
		return numLUNs;
	}

	public void setNumLuns(long numLUNs) {
		this.numLUNs = numLUNs;
	}
	
	public int getNumCopies() {
		return numCopies;
	}

	public Mapping[][] getMappings() {
		return mappings;
	}

	public long getChunkSize() {
		return chunkSize;
	}

	public int getNumChunks() {
		return numChunks;
	}

	public void setId(int id) {
		this.id = id;
	}
}
