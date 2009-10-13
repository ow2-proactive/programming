@rem replace the dummy path below by the current proactivemain.py location
python "path\to\ProActive\scripts\virtualization\proactivemain.py" proactive-runtime.log
@rem you can now specify this kind of batch file as a daemon on a Windows host using Sc & autoexnt.
@rem you just have to add the path of this file within your autoexnt.bat file wich is situated in C:\WINDOWS\system32\autoexnt.bat