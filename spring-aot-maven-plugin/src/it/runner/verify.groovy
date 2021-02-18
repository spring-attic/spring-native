def file = new File(basedir, "build.log")
return file.text.contains("ApplicationRunner is executed.")