!define PRODUCT_NAME "Storage@Desk"
!define PRODUCT_VERSION "1.0"
!define PRODUCT_PUBLISHER "SAD Systems"
!define PRODUCT_WEB_SITE "http://code.google.com/p/storage-at-desk/"
!define PRODUCT_UNINST_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}"
!define PRODUCT_UNINST_ROOT_KEY "HKLM"

; MUI 1.67 compatible ------
!include "MUI.nsh"

; MUI Settings
!define MUI_ABORTWARNING
!define MUI_ICON "sd.ico"
!define MUI_UNICON "uninst.ico"

; Welcome page
!insertmacro MUI_PAGE_WELCOME
; License page
!insertmacro MUI_PAGE_LICENSE "gpl.txt"
; Directory page
!insertmacro MUI_PAGE_DIRECTORY
; Instfiles page
!insertmacro MUI_PAGE_INSTFILES
; Finish page
!define MUI_FINISHPAGE_RUN "$SYSDIR\javaw.exe" "-jar $INSTDIR\sdtray.jar"
!insertmacro MUI_PAGE_FINISH

; Uninstaller pages
!insertmacro MUI_UNPAGE_INSTFILES

; Language files
!insertmacro MUI_LANGUAGE "English"

; MUI end ------

Name "${PRODUCT_NAME} ${PRODUCT_VERSION}"
OutFile "Setup.exe"
InstallDir "$PROGRAMFILES\SAD"
ShowInstDetails show
ShowUnInstDetails show

Section "MainSection" SEC01
  SetOutPath "$INSTDIR"
  SetOverwrite ifnewer
  File "config.jar"
  File "sd_active.png"
  File "sd_inactive.png"
  File "sdtray.jar"
  CreateDirectory "$INSTDIR\Storage"
  CreateShortCut "$DESKTOP.lnk" "$INSTDIR\sdtray.jar"
  File "storage@desk.log4j.properties"
  File "storage@desk.properties.xml"
  File "StorageMachine.jar"
  File "StorageServer.jar"
  File "VolumeController.jar"
SectionEnd

Section -AdditionalIcons
  CreateDirectory "$SMPROGRAMS\Storage@Desk"
  CreateShortCut "$SMPROGRAMS\Storage@Desk\Uninstall.lnk" "$INSTDIR\uninst.exe"
SectionEnd

Section -Post
  WriteUninstaller "$INSTDIR\uninst.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayName" "$(^Name)"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "UninstallString" "$INSTDIR\uninst.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayVersion" "${PRODUCT_VERSION}"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "URLInfoAbout" "${PRODUCT_WEB_SITE}"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "Publisher" "${PRODUCT_PUBLISHER}"
SectionEnd


Function un.onUninstSuccess
  HideWindow
  MessageBox MB_ICONINFORMATION|MB_OK "$(^Name) was successfully removed from your computer."
FunctionEnd

Function un.onInit
  MessageBox MB_ICONQUESTION|MB_YESNO|MB_DEFBUTTON2 "Are you sure you want to completely remove $(^Name) and all of its components?" IDYES +2
  Abort
FunctionEnd

Section Uninstall
  Delete "$INSTDIR\uninst.exe"
  Delete "$INSTDIR\VolumeController.jar"
  Delete "$INSTDIR\StorageServer.jar"
  Delete "$INSTDIR\StorageMachine.jar"
  Delete "$INSTDIR\storage@desk.properties.xml"
  Delete "$INSTDIR\storage@desk.log4j.properties"
  Delete "$INSTDIR\sdtray.jar"
  Delete "$INSTDIR\sd_inactive.png"
  Delete "$INSTDIR\sd_active.png"
  Delete "$INSTDIR\config.jar"

  Delete "$SMPROGRAMS\Storage@Desk\Uninstall.lnk"
  Delete "$DESKTOP.lnk"

  RMDir "$SMPROGRAMS\Storage@Desk"
  RMDir "$INSTDIR"
  RMDir ""

  DeleteRegKey ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}"
  SetAutoClose true
SectionEnd