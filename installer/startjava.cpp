// startjava.cpp : Defines the entry point for the console application.
//

//include "stdafx.h"
#include <windows.h>
#include <shellapi.h>
#include <stdlib.h>
#include <stdio.h>

int main(int argc, _TCHAR* argv[])
{
	int iReturn = (int) ShellExecute(NULL, "open", "sdtray.jar", NULL, NULL, SW_SHOWNORMAL);
	// If ShellExecute returns an error code, let the user know.

	if (iReturn <= 32)
	{
		//MessageBox ("Cannot open file. File may have been moved or deleted.", "Error!", MB_OK | MB_ICONEXCLAMATION) ;
	}

	return 0;
}
