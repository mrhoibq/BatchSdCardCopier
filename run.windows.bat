SETCONSOLE /minimize
CONSOLESTATE /hide

:: Use WMIC to retrieve date and time
FOR /F "skip=1 tokens=1-6" %%G IN ('WMIC Path Win32_LocalTime Get Day^,Hour^,Minute^,Month^,Second^,Year /Format:table') DO (
   IF "%%~L"=="" goto s_done
      Set _yyyy=%%L
      Set _mm=00%%J
      Set _dd=00%%G
      Set _hour=00%%H
      SET _minute=00%%I
)
:s_done

:: Pad digits with leading zeros
      Set _mm=%_mm:~-2%
      Set _dd=%_dd:~-2%
      Set _hour=%_hour:~-2%
      Set _minute=%_minute:~-2%

:: Display the date/time in yyyymmdd format
Set today=%_yyyy%%_mm%%_dd%
Echo %today%

set log_file=output_%today%.log

:: Get OS info 
wmic os get buildnumber,caption,CSDVersion,osarchitecture /Format:list 1>>os.log 2>&1
type os.log 1>>%log_file% 2>&1
del /F /Q os.log

%~dp0/jre-10.0.1-windows/bin/java -version -d64 1>>%log_file% 2>&1
%~dp0/jre-10.0.1-windows/bin/java -jar BatchSdCardCopier.jar 1>>%log_file% 2>&1