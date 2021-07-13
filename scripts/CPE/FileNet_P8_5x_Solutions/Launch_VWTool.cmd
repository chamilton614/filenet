@echo off
title vwtool

set ConnectionPoint=CPEOS1DV_CP1
set Username=cpeadmin-dv
set Password=P@ssw0rd

cd /D C:\IBM\FileNet\ContentEngine\tools\PE
REM vwtool <Connection Point> -Y <Username>+<Password>
vwtool %ConnectionPoint% -Y %Username%+%Password%