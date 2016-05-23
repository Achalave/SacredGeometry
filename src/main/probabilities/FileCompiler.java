package main.prob;



//@author Michael Haertling

public class FileCompiler {
    
    public FileCompiler(){
        
    }
    
    //    public void finalizeFile() {
//        //Make sure the file has been finished
//        if (!didFinish) {
//            System.err.println("ProbabilityCalculator could not finalize file: unfinished");
//            return;
//        }
//
//        didFinish = false;
//
//        String path = FileManager.getProbabilityPathIncomplete(numDie, d8);
//        String pathNew = FileManager.getProbabilityPath(numDie, d8);
//        String[] files = FileManager.getFiles(path);
//        //It was a silgle file
//        if (files == null) {
//            //Simply rename the file
//            FileManager.renameFile(path, pathNew);
//        } //It was multithreaded and has multiple files
//        else {
//            try (PrintWriter writer = FileManager.openFileForWriting(pathNew)) {
//                boolean lineOne = true;
//                //Combine all the files into one
//                for (String file : files) {
//                    try (Scanner scan = FileManager.openFileForReading(path + file)) {
//                        while (scan.hasNextLine()) {
//                            if (lineOne) {
//                                lineOne = false;
//                                writer.print(scan.nextLine());
//                            } else {
//                                writer.print("\n" + scan.nextLine());
//                            }
//                        }
//                    }
//                    FileManager.deleteFile(path + file);
//                }
//                FileManager.deleteFile(path);
//            }
//        }
//    }
}
