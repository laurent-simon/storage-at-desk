package edu.virginia.cs.storagedesk.database;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;

import org.apache.log4j.Logger;

public class Machine implements Externalizable {
	
	private static Logger logger = Logger.getLogger(Machine.class);
	
	private static final long serialVersionUID = 0;
	
	private String id;
	private String name;
	private String ip;	
	private String path;
	private int numChunks;
	private long chunkSize;
	
	public Machine() {
		
	}
	
	public Machine(String id) {
		this.id = id;
	}
	
	public Machine(String id, String hostName, 
					String ip, String path,
					int num, long size) {
		this.id = id;
		this.name = hostName;
		this.ip = ip;
		this.path = path;
		this.chunkSize = size;
		this.numChunks = num;
		
		logger.info("Machine id " + id + 
				" name " + hostName +
				" ip " + ip + 
				" path " + path +
				" chunk num " + num +
				" chunk size " + size);
	}

	public Machine(String id, String hostName, String ip, int num, long size) {
		this.id = id;
		this.name = hostName;
		this.ip = ip;
		this.chunkSize = size;
		this.numChunks = num;
			
		logger.info("Machine id " + id + 
				    " hostname " + hostName +
		            " ip " + ip + 
		            " chunk num " + num +
				    " chunk size " + size);
	}

	public boolean inDatabase(Connection conn) {
		ResultSet rs = null;
		PreparedStatement stmt = null;
		try { 
			logger.debug("call sp_machinebyid('" + this.id + "')");
			
	        stmt = conn.prepareStatement("{ call sp_machinebyid(?) }");
	        stmt.setString(1, this.id);
	        rs = stmt.executeQuery();
	        
            if (rs.next()) {
                
            	// Exists in the DB
            	// Need to verify the information
            	
            	// How about ip address?
            	// Take the latest information for now
            	// But this is a security risk
            	logger.info("Exists a machine (id " + this.id +")");
            	
                if (rs.next()) {
                	logger.error("Too many machines with the same (id " + this.id +")");
                }
        		
            } else {
            	logger.info("No machine (id " + this.id +")");
            	return false;
            }
        }
        catch (Exception e) {
        	e.printStackTrace();
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
		return true;
	}
	
	public boolean heartbeat(Connection conn) {
		logger.debug("To update heart beat (id " + this.id +")");
		PreparedStatement stmt = null;
		try { 
	        stmt = conn.prepareStatement("{ call sp_machineheartbeat(?) }");
	        stmt.setString(1, this.id);
	        stmt.execute();
	        return true;	        
        }
        catch (Exception e) {
        	logger.error(e.toString());
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
	
	public boolean insert(Connection conn) {
		logger.debug("To insert machine (id " + this.id +")");
		PreparedStatement stmt = null;
		try { 
//			 Call a procedure with one IN parameter
	        stmt = conn.prepareStatement("{ call sp_insertmachine(?, ?, ?, ?, ?, ?) }");
	    
	        // Set the value for the IN parameter
	        stmt.setString(1, this.id);
	        stmt.setString(2, this.name);
	        stmt.setString(3, this.ip);
	        stmt.setString(4, this.path);
	        stmt.setInt(5, this.numChunks);
	        stmt.setLong(6, this.chunkSize);
	        
	        return stmt.execute();
	        
        }
        catch (Exception e) {
        	logger.error(e.toString());
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
	
	public static Hashtable<String, Machine> getAvailableMachines(Connection conn, 
															      long chunkSize) {
		logger.info("Get all available machines whose chunk size is " + chunkSize);
		
		Hashtable<String, Machine> machines = new Hashtable<String, Machine>();
		ResultSet rs = null;
		PreparedStatement stmt = null;
		try { 
	        stmt = conn.prepareStatement("{ call sp_availablemachines(?) }");
	        stmt.setLong(1, chunkSize);
	        rs = stmt.executeQuery();
            while (rs.next()) {
            	String id = rs.getString("id");
            	machines.put(id, 
            			     new Machine(id, 
            			    		     rs.getString("name"),
            			    		     rs.getString("ip"),
            			    		     rs.getInt("numchunks"),
            			    		     rs.getInt("chunksize")));
            } 
        } catch (Exception e) {
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
		return machines;
	}
	
	public static Machine getMachine(Connection conn, String id) {
		ResultSet rs = null;
		Statement stmt = null;
		try {         
            stmt = conn.createStatement();
            stmt.executeQuery("SELECT id, name, ip, numChunk, chunksize FROM machine " +
            		          "WHERE id = '" + id + "'");
            rs = stmt.getResultSet();
            if (rs.next()) {
            	return new Machine(rs.getString("id"), 
    			    		       rs.getString("ame"),
    			    		       rs.getString("ip"),
    			    		       rs.getInt("numchunk"),
    			    		       rs.getInt("chunksize"));

            } 
        } catch (Exception e) {
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
    
		return null;
	}
	
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		logger.trace("Machine: Read external ...");
		
		this.id       = in.readUTF();
		this.name 	  = in.readUTF();
		this.ip       = in.readUTF();
		this.path 	  = in.readUTF();
		this.numChunks= in.readInt();
		this.chunkSize = in.readLong();
		
		logger.trace("Machine: Read external completes");
	}
	
	public void writeExternal(ObjectOutput out) throws IOException {
		logger.trace("Machine: Write external ...");
		
		out.writeUTF(this.id);
		out.writeUTF(this.name);
		out.writeUTF(this.ip);
		out.writeUTF(this.path);
		out.writeInt(this.numChunks);
		out.writeLong(this.chunkSize);
		
		logger.trace("Machine: Write external completes");
	}
	
	
	public long getChunkSize() {
		return chunkSize;
	}

	public String getName() {
		return name;
	}

	public void setName(String hostName) {
		this.name = hostName;
	}

	public String getId() {
		return id;
	}

	public String getIp() {
		return ip;
	}

	public int getNumAvailableChunks(Connection conn) {
		ResultSet rs = null;
		PreparedStatement stmt = null;
		try { 
	        stmt = conn.prepareStatement("{ call sp_machinenumavailablechunks(?) }");
	        stmt.setString(1, this.id);
	        rs = stmt.executeQuery();
	        
            if (rs.next()) {
            	return rs.getInt("numchunks");
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
	
	public int getAvailableChunk(Connection conn) {
		ResultSet rs = null;
		PreparedStatement stmt = null;
		try { 
	        stmt = conn.prepareStatement("{ call sp_availablechunk(?) }");
	        stmt.setString(1, this.id);
	        rs = stmt.executeQuery();
	        
            if (rs.next()) {
            	return rs.getInt("availablechunk");
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

	public int getNumChunks() {
		return numChunks;
	}

	public String getPath() {
		return path;
	}
}
