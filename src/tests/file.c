#include <stdio.h>

void main()
{
  int * b, *b1;
  int i, pos, size;
  FILE *f = fopen("1.txt", "wb+" );
  pos = 0;
  size = 30;
  b = (int *) malloc(size * sizeof(int));
  for (i = 0; i < size; i++)
  	b[i] = i;
  
  fseek(f, pos, SEEK_SET);
  	
  fwrite(b, sizeof(int), size, f);
  fflush(f);
  
  fseek(f, pos, SEEK_SET);
  b1 = (int *) malloc(size * sizeof(int));
  fread(b1, sizeof(int), size, f);
  
  for (i = 0; i < size; i++) {
  	if (b[i] != b1[i])
  		printf("values at %d : %d - %d\n", i, b[i], b1[i]);	
  }
  
  fclose(f);
}