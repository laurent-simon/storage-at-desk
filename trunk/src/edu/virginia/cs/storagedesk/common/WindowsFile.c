#include <jni.h>
#include <stdio.h>
#include <windows.h>

JNIEXPORT jboolean JNICALL Java_edu_virginia_cs_storagedesk_common_NativeFile_write
  (JNIEnv * env, jobject jobj, jstring name, jbyteArray bytes, jlong pos) {
  HANDLE hFile;
  DWORD dwBytesWritten;
  jbyte * b;
  jsize size;
  jboolean iscopy, t;
  BOOL fSuccess;
  const char *cfile;
  LPCTSTR lpName;
  
  printf("windows write\n");
  
  cfile = (*env)->GetStringUTFChars(env, name, &iscopy);
  lpName = cfile;
  
  hFile = CreateFile(lpName,        // file name 
                   GENERIC_WRITE,         // open for writing 
                   0,      // 
                   NULL,                  // default security 
                   OPEN_EXISTING,         // existing file only 
                   FILE_ATTRIBUTE_NORMAL|FILE_FLAG_NO_BUFFERING|FILE_FLAG_WRITE_THROUGH, // normal file 
                   NULL);                 // no template 
  
   if (hFile == INVALID_HANDLE_VALUE) 
    { 
        printf ("CreateFile failed with error %d.\n", 
                GetLastError());
        return (1);
    } 
    
   SetFilePointer(hFile, pos, NULL, FILE_BEGIN);
   size = (*env)->GetArrayLength(env, bytes);
   b = (*env)->GetByteArrayElements(env, bytes, &iscopy);
   fSuccess = WriteFile(hFile, 
                                 b, 
                                 size,
                                 &dwBytesWritten, 
                                 NULL); 
    if (!fSuccess) 
    {
        printf ("WriteFile failed with error %d.\n", 
                 GetLastError());
        return (5);
    }
    
    fSuccess = CloseHandle (hFile);
    if (!fSuccess) 
    {
       printf ("CloseHandle failed with error %d.\n", 
               GetLastError());
       return (7);
    }
              
  printf("windows writing done\n");
    
    (*env)->ReleaseStringUTFChars(env, name, cfile);
    
   t = 0;
   return t;
}

JNIEXPORT jbyteArray JNICALL Java_edu_virginia_cs_storagedesk_common_NativeFile_read
  (JNIEnv * env, jobject jobj, jstring name, jlong pos, jint len) {
  	
  	HANDLE hFile;
  DWORD dwBytesRead;
  jbyte * b;
  jsize size;
  jboolean iscopy, t;
  BOOL fSuccess;
  jbyteArray jb;
    
  const char *cfile;
  LPCTSTR lpName;
  
  printf("windows read\n");
  
  cfile = (*env)->GetStringUTFChars(env, name, &iscopy);
  lpName = cfile;
  
  hFile = CreateFile(lpName,        // file name 
                   GENERIC_READ,          // open for reading 
                   0,       				// share 
                   NULL,                  // default security 
                   OPEN_EXISTING,         // existing file only 
                   FILE_ATTRIBUTE_NORMAL|FILE_FLAG_NO_BUFFERING|FILE_FLAG_WRITE_THROUGH, // normal file 
                   NULL);                 // no template 
  
   if (hFile == INVALID_HANDLE_VALUE) 
    { 
        printf ("CreateFile failed with error %d.\n", 
                GetLastError());
        return NULL;
    } 
    
   SetFilePointer(hFile, pos, NULL, FILE_BEGIN);
   
   size = len;
   b = (jbyte *) malloc(len * sizeof(jbyte));
  
   fSuccess = ReadFile(hFile, 
                                 b, 
                                 size,
                                 &dwBytesRead, 
                                 NULL); 
    if (!fSuccess) 
    {
        printf ("ReadFile failed with error %d.\n", 
                 GetLastError());
        return NULL;
    }
    
    fSuccess = CloseHandle (hFile);
    if (!fSuccess) 
    {
       printf ("CloseHandle failed with error %d.\n", 
               GetLastError());
       return NULL;
    }
              
  printf("windows reading done\n");
      
  (*env)->ReleaseStringUTFChars(env, name, cfile);
  
  jb = (*env)->NewByteArray(env, size);
  (*env)->SetByteArrayRegion(env, jb, 0, len, b);
  return (jb); 
}