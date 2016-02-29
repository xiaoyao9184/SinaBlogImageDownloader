nowpath = CreateObject("Scripting.FileSystemObject").GetFile(Wscript.ScriptFullName).ParentFolder.Path
nowpath = """" + nowpath  + "\SinaBlogImageDownloader.BAT"""

Set ws = CreateObject("Wscript.Shell") 
ws.run "cmd /c " + nowpath,vbhide