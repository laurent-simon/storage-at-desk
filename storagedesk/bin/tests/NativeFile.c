#include <jni.h>
#include <stdio.h>
#include "edu_virginia_cs_storagedesk_common_NativeFile.h"

JNIEXPORT jboolean JNICALL Java_edu_virginia_cs_storagedesk_common_NativeFile_write
  (JNIEnv * env, jobject jobj, jstring name, jbyteArray bytes, jlong pos) {
  jboolean iscopy, t;
  jbyte * b, * b1;
  jsize size;
  int i;
  
  const char *mfile = (*env)->GetStringUTFChars(env, name, &iscopy);
  
  FILE *f = fopen(mfile, "rb+" );
  
  printf("native write\n");
  
  fseek(f, pos, SEEK_SET);
  size = (*env)->GetArrayLength(env, bytes);
  b = (*env)->GetByteArrayElements(env, bytes, &iscopy);
  fwrite(b, sizeof(jbyte), size, f);
//  fflush(f);

  fclose(f);

  printf("native writing done\n");
    
  f = fopen(mfile, "rb");
  
  fseek(f, pos, SEEK_SET);
  b1 = (jbyte *) malloc(size * sizeof(jbyte));
  fread(b1, sizeof(jbyte), size, f);
  
  for (i = 0; i < size; i++) {
    if (b[i] != b1[i])
  		printf("error at %d : %d - %d\n", i, b[i], b1[i]);	
  }
  
  fclose(f);
   
   (*env)->ReleaseStringUTFChars(env, name, mfile);
  
   printf("comparison done\n");
  
   t = 1;
   return t;
}

JNIEXPORT jbyteArray JNICALL Java_edu_virginia_cs_storagedesk_common_NativeFile_read
  (JNIEnv * env, jobject jobj, jstring name, jlong pos, jint len) {
  jboolean iscopy;
  jsize size;
  jbyteArray jb;
  jbyte * b;
    
  const char *mfile = (*env)->GetStringUTFChars(env, name, &iscopy);
  
  FILE *f = fopen(mfile, "rb+");
  printf("native read\n");  
  fseek(f, pos, SEEK_SET);
  size = len;
  b = (jbyte *) malloc(len * sizeof(jbyte));
  fread(b, sizeof(jbyte), size, f);
  fclose(f);  
  printf("native read done\n");
  
  (*env)->ReleaseStringUTFChars(env, name, mfile);
  
  jb = (*env)->NewByteArray(env, size);
  (*env)->SetByteArrayRegion(env, jb, 0, len, b);
  return (jb); 
}