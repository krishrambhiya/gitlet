package gitlet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author Krish Rambhiya
 */
public class Repository {
    private static String HEAD;
    private static ArrayList<String> commits = new ArrayList();
    private static HashMap<String, String> branches = new HashMap<>();
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");


    public static final File CWD = new File(System.getProperty("user.dir"));
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    private static String commitCurrently;

    public static void initializedCheck() {
        List<String> filesInitial = Utils.plainFilenamesIn(GITLET_DIR);
        if (filesInitial == null) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    public static void init() {
        List<String> filesDirectory = Utils.plainFilenamesIn(GITLET_DIR);
        helperInit(filesDirectory);
    }

    public static void helperInit(List<String> filesDirectory){
        if (!(filesDirectory == null)) {
            System.out.println("gitlet vc already in place.");
            System.exit(0);
        } else {
            GITLET_DIR.mkdir();

            Commit commitInitial = new Commit("initial commit", new Date(0));
            byte[] byteCommitInitial = Utils.serialize(commitInitial);
            String hashCommitInitial = Utils.sha1(byteCommitInitial, "commit");

            File fileCommitInitial = Utils.join(GITLET_DIR, hashCommitInitial);
            Utils.writeObject(fileCommitInitial, commitInitial);

            String top = hashCommitInitial;
            commits.add(hashCommitInitial);
            branches.put("master", hashCommitInitial);
            HEAD = top;
            String branchCurrently = "master";

            File fileBranch = Utils.join(GITLET_DIR, "branches");
            Utils.writeObject(fileBranch, branches);
            File fileBranchCurrently = Utils.join(GITLET_DIR, "HEAD.txt");
            Utils.writeContents(fileBranchCurrently, HEAD);
            File fileNameBranchCurrently = Utils.join(GITLET_DIR, "currentBranchName.txt");
            Utils.writeContents(fileNameBranchCurrently, branchCurrently);
            File fileOfCommits = Utils.join(GITLET_DIR, "commits");
            Utils.writeObject(fileOfCommits, commits);

            helperInit2();
        }
    }

    public static void helperInit2(){
        HashMap<String,String> filesForRemove = new HashMap<>();
        HashMap<String, String> filesForAdd = new HashMap<>();

        File stagedForAdditionFile = Utils.join(GITLET_DIR, "stagedForAddition");
        File stagedForRemovalFile = Utils.join(GITLET_DIR, "stagedForRemoval");

        Utils.writeObject(stagedForRemovalFile, filesForRemove);
        Utils.writeObject(stagedForAdditionFile, filesForAdd);
    }


    public static void add(String fileName) {
        initializedCheck();
        List<String> fileUsed = Utils.plainFilenamesIn(CWD);
        if (!(fileUsed.contains(fileName))) {
            System.out.print("File does not exist.");
            System.exit(0);
        } else {
            helperAdd1(fileName);
        }
    }

    public static void helperAdd1(String fileName){
        addHelper3(fileName);
        addHelper2(fileName);
    }

    public static void addHelper2(String fileName){
        File fileForRemoval = Utils.join(GITLET_DIR, "stagedForRemoval");
        HashMap hashmapForRemoval =
                Utils.readObject(fileForRemoval, HashMap.class);
        if (hashmapForRemoval.containsKey(fileName)) {
            hashmapForRemoval.remove(fileName);
        }
        Utils.writeObject(fileForRemoval, hashmapForRemoval);
    }

    public static void addHelper3(String fileName){
        File additionalFile = Utils.join(CWD, fileName);
        byte[] byteAdditionalFile = Utils.readContents(additionalFile);
        String hashAdditionalFile = Utils.sha1(byteAdditionalFile, "blob");

        File fileStagedAdding = Utils.join(GITLET_DIR, "stagedForAddition");
        HashMap hashForAdding = Utils.readObject(fileStagedAdding, HashMap.class);
        File headFile = Utils.join(GITLET_DIR, "HEAD.txt");
        String head = Utils.readContentsAsString(headFile);
        File fileHeadCurrently = Utils.join(GITLET_DIR, head);
        Commit commitCurrently = Utils.readObject(fileHeadCurrently, Commit.class);

        if (!(commitCurrently.getFileBlob().containsKey(fileName))) {
            File blobAdditionalFile = Utils.join(GITLET_DIR, hashAdditionalFile);
            Utils.writeContents(blobAdditionalFile, byteAdditionalFile);
            hashForAdding.put(fileName, hashAdditionalFile);
        } else {
            if (!(commitCurrently.getFileBlob().get(fileName).equals(hashAdditionalFile))) {
                File blobNewFile = Utils.join(GITLET_DIR, hashAdditionalFile);
                Utils.writeContents(blobNewFile, byteAdditionalFile);
                hashForAdding.put(fileName, hashAdditionalFile);
            } else {
                hashForAdding.remove(fileName);
            }
        }
        Utils.writeObject(fileStagedAdding, hashForAdding);
    }



    public static void commit(String message) {
        initializedCheck();
        if (!(message.equals(""))) {
            helperCommit1(message);
        } else {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
    }

    public static void helperCommit1(String message){
        File fileForRemoval = Utils.join(GITLET_DIR, "stagedForRemoval");
        HashMap stagedForRemoval = Utils.readObject(fileForRemoval, HashMap.class);
        File fileForAddition = Utils.join(GITLET_DIR, "stagedForAddition");
        HashMap stagedForAddition = Utils.readObject(fileForAddition, HashMap.class);

        helperCommit2(stagedForAddition, stagedForRemoval);
        File fileOnTop = Utils.join(GITLET_DIR, "HEAD.txt");
        String head = Utils.readContentsAsString(fileOnTop);
        File headCurrently = Utils.join(GITLET_DIR, head);
        Commit commitCurrently = Utils.readObject(headCurrently, Commit.class);
        Commit commitNewly = new Commit(message, new Date(), commitCurrently.getFileBlob(), head);

        for (Object i: stagedForRemoval.keySet()) {
            commitNewly.getFileBlob().remove(i);
        }

        for (Object i: stagedForAddition.keySet()) {
            commitNewly.getFileBlob().put(i, stagedForAddition.get(i));
        }

        stagedForRemoval.clear();
        stagedForAddition.clear();

        Utils.writeObject(fileForRemoval, stagedForRemoval);
        Utils.writeObject(fileForAddition, stagedForAddition);

        helperCommit3(commitNewly, fileOnTop);
    }

    public static void helperCommit2( HashMap stagedForAddition,  HashMap stagedForRemoval){
        if (stagedForAddition.size() == 0 && stagedForRemoval.size() == 0) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
    }

    public static void helperCommit3(Commit commitNewly,  File fileOnTop){
        byte[] byteCommitNewly = Utils.serialize(commitNewly);
        String hashCommitNewly = Utils.sha1(byteCommitNewly, "commit");
        File fileCommitNewly = Utils.join(GITLET_DIR, hashCommitNewly);
        Utils.writeObject(fileCommitNewly, commitNewly);
        Utils.writeContents(fileOnTop, hashCommitNewly);

        ArrayList<String> listOfCommits = Utils.readObject(Utils.join(GITLET_DIR, "commits"), ArrayList.class);
        listOfCommits.add(hashCommitNewly);
        Utils.writeObject(Utils.join(GITLET_DIR, "commits"), listOfCommits);

        File branchNameCurrently = Utils.join(GITLET_DIR, "currentBranchName.txt");
        String currentBranchName = Utils.readContentsAsString(branchNameCurrently);
        File fileMapOfBranches = Utils.join(GITLET_DIR, "branches");
        HashMap mapOfBranches = Utils.readObject(fileMapOfBranches, HashMap.class);
        mapOfBranches.put(currentBranchName, hashCommitNewly);
        Utils.writeObject(fileMapOfBranches, mapOfBranches);  System.exit(0);
    }

    public static void checkout(String fileName) {
        initializedCheck();
        File fileOnTop = Utils.join(GITLET_DIR, "HEAD.txt");
        String head = Utils.readContentsAsString(fileOnTop);
        Commit commitCurrently = Utils.readObject(Utils.join(GITLET_DIR, head), Commit.class);
        helperCheckout1(commitCurrently, fileName);
    }

    public static void helperCheckout1(Commit commitCurrently, String fileName){
        if ((commitCurrently.getFileBlob().containsKey(fileName))) {
            String strRecovered = (String) commitCurrently.getFileBlob().get(fileName);
            byte[] recoveryOfFile = Utils.readContents(Utils.join(GITLET_DIR, strRecovered));
            File additionOfFile = Utils.join(CWD, fileName);
            Utils.writeContents(additionOfFile, recoveryOfFile);
        } else {
            System.out.println("file not in this respective commit.");
            System.exit(0);
        }
    }

    public static void checkoutWithCommitID(String commitID, String fileName) {
        initializedCheck();
        ArrayList commitsBefore = Utils.readObject(Utils.join(GITLET_DIR, "commits"), ArrayList.class);
        Iterator<String> iteratorFirst = commitsBefore.iterator();
        int size = commitID.length();
        if (size < 40) {
            while (iteratorFirst.hasNext()) {
                String fileUpcoming = iteratorFirst.next();
                if (fileUpcoming.length() >= size && fileUpcoming.substring(0, size).equals(commitID)) {
                    commitID = fileUpcoming;
                }
            }
        }
        helperCheckoutWithCommitID1(commitsBefore, commitID, fileName);
    }

    public static void helperCheckoutWithCommitID1(ArrayList commitsBefore, String commitID, String fileName){
        if (!(commitsBefore.contains(commitID))) {
            System.out.println("No commit with that id exists.");
        } else {
            Commit commitCurrently = Utils.readObject(Utils.join(GITLET_DIR, commitID), Commit.class);

            if (!(commitCurrently.getFileBlob().containsKey(fileName))) {
                System.out.println("File does not exist in that commit.");
            } else {
                String strRecovered = (String) commitCurrently.getFileBlob().get(fileName);
                byte[] recoveryFile = Utils.readContents(Utils.join(GITLET_DIR, strRecovered));
                File toBeAddedFile = Utils.join(CWD, fileName);
                Utils.writeContents(toBeAddedFile, recoveryFile);
            }
        }
    }



    public static void checkoutBranch(String branchName) {
        helperCheckoutBranch1(branchName);
    }

    public static void helperCheckoutBranch1(String branchName){
        List<String> dictionaryFiles= Utils.plainFilenamesIn(CWD);
        File fileOnTop = Utils.join(GITLET_DIR, "HEAD.txt");
        String head = Utils.readContentsAsString(fileOnTop);
        File fileHeadCurrently= Utils.join(GITLET_DIR, head);
        Commit commitCurrently = Utils.readObject(fileHeadCurrently, Commit.class);

        File directoryBranches = Utils.join(GITLET_DIR, "branches");
        HashMap mapOfBranches = Utils.readObject(directoryBranches, HashMap.class);
        File nameCurrentBranch = Utils.join(GITLET_DIR, "currentBranchName.txt");
        String strCurrentBranch = Utils.readContentsAsString(nameCurrentBranch);

        if (!mapOfBranches.containsKey(branchName)) {
            System.out.println("No such branch exists.");
        } else if (branchName.equals(strCurrentBranch)) {
            System.out.println("No need to checkout the current branch.");
        } else {
            Commit commitOfBranch = Utils.readObject(Utils.join(GITLET_DIR, (String) mapOfBranches.get(branchName)), Commit.class);
            Iterator<String> branchIterator = dictionaryFiles.iterator();

            while (branchIterator.hasNext()) {
                String nextFileInBranch = branchIterator.next();
                if (!commitCurrently.getFileBlob().containsKey(nextFileInBranch)
                        && commitOfBranch.getFileBlob().containsKey(nextFileInBranch)) {
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                } else if (commitCurrently.getFileBlob().containsKey(nextFileInBranch) && !commitOfBranch.getFileBlob().containsKey(nextFileInBranch)) {
                    Utils.restrictedDelete(Utils.join(CWD, nextFileInBranch));
                }
            }

            Iterator<String> iteratorBlobBranch = commitOfBranch.getFileBlob().keySet().iterator();
            while (iteratorBlobBranch.hasNext()) {
                String blobOfBranch = iteratorBlobBranch.next();
                File fileOfBranch = Utils.join(CWD, blobOfBranch);
                Utils.writeContents(fileOfBranch, Utils.readContents(Utils.join(GITLET_DIR, (String) commitOfBranch.getFileBlob().get(blobOfBranch))));
            }

            File fileForAddition = Utils.join(GITLET_DIR, "stagedForAddition");
            HashMap stagedForAddition = Utils.readObject(fileForAddition, HashMap.class);
            File fileForRemoval = Utils.join(GITLET_DIR, "stagedForRemoval");
            HashMap stagedForRemoval = Utils.readObject(fileForRemoval,HashMap.class);
            stagedForAddition.clear();
            stagedForRemoval.clear();

            Utils.writeObject(fileForAddition, stagedForAddition);
            Utils.writeObject(fileForRemoval, stagedForRemoval);
            Utils.writeContents(fileOnTop, mapOfBranches.get(branchName));
            Utils.writeContents(nameCurrentBranch, branchName);
        }
    }

    public static void log() {
        initializedCheck();
        File fileOnTop = Utils.join(GITLET_DIR, "HEAD.txt");
        String head = Utils.readContentsAsString(fileOnTop);
        Commit commitCurrently = Utils.readObject(Utils.join(GITLET_DIR, head), Commit.class);
        helperLog1(commitCurrently, head);
    }

    public static void helperLog1(Commit commitCurrently, String head){
        while (head != null) {
            commitCurrently = Utils.readObject(Utils.join(GITLET_DIR, head), Commit.class);
            System.out.println("===");
            System.out.println("commit " + head);
            System.out.println("Date: " + dateFormat.format(commitCurrently.getTimestamp()));
            System.out.println(commitCurrently.getMessage());
            System.out.println();
            head = commitCurrently.getParent();
        }
    }



    public static void rm(String fileName) {
        initializedCheck();

        File removalFile = Utils.join(CWD, fileName);
        List<String> directoryOfFiles = Utils.plainFilenamesIn(CWD);

        File fileAdding = Utils.join(GITLET_DIR, "stagedForAddition");
        HashMap stagedForAddition = Utils.readObject(fileAdding, HashMap.class);

        File fileRemoving = Utils.join(GITLET_DIR, "stagedForRemoval");
        HashMap stagedForRemoval = Utils.readObject(fileRemoving, HashMap.class);

        File headFile = Utils.join(GITLET_DIR, "HEAD.txt");
        String head = Utils.readContentsAsString(headFile);
        File fileHeadCurrently = Utils.join(GITLET_DIR, head);
        Commit commitCurrently = Utils.readObject(fileHeadCurrently, Commit.class);

        helperRemove1(stagedForAddition,  directoryOfFiles, stagedForRemoval, fileRemoving,
                commitCurrently, fileAdding, fileName, removalFile);
    }

    public static void helperRemove1(HashMap stagedForAddition, List<String> filesInDirectory, HashMap stagedForRemoval,
                                     File stagedForRemovalFile, Commit currentCommit, File stagedForAdditionFile, String fileName, File fileToBeRemoved){
        if (stagedForAddition.containsKey(fileName) || currentCommit.getFileBlob().containsKey(fileName)) {
            if (stagedForAddition.containsKey(fileName)) {
                stagedForAddition.remove(fileName);
                Utils.writeObject(stagedForAdditionFile, stagedForAddition);
            }
            if (currentCommit.getFileBlob().containsKey(fileName)) {
                if (filesInDirectory.contains(fileName)){
                    byte[] removedFileByte = Utils.readContents(fileToBeRemoved);
                    String removedFileHash = Utils.sha1(removedFileByte, "blob");
                    stagedForRemoval.put(fileName, removedFileHash);
                    Utils.writeObject(stagedForRemovalFile, stagedForRemoval);
                    Utils.restrictedDelete(Utils.join(CWD, fileName));
                } else {
                    stagedForRemoval.put(fileName, "");
                    Utils.writeObject(stagedForRemovalFile, stagedForRemoval);
                }
            }
        } else {
            System.out.println("No reason to remove the file.");
        }
    }


    public static void global_log() {
        ArrayList<String> commits = Utils.readObject(Utils.join(GITLET_DIR, "commits"), ArrayList.class);
        Iterator<String> iteratorOfCommits = commits.iterator();

        helperGlobalLog1(iteratorOfCommits);
    }

    public static void helperGlobalLog1(Iterator<String> iteratorOfCommits){
        while (iteratorOfCommits.hasNext()) {
            String hashOfCommits = iteratorOfCommits.next();
            File fileOfCommits = Utils.join(GITLET_DIR, hashOfCommits);
            Commit commit = Utils.readObject(fileOfCommits, Commit.class);
            System.out.println("===");
            System.out.println("commit " + hashOfCommits);
            System.out.println("Date: " + dateFormat.format(commit.getTimestamp()));
            System.out.println(commit.getMessage());
            System.out.println("");
        }
    }

    public static void branch(String branchName) {
        branchHelper1(branchName);
    }

    public static void branchHelper1(String branchName){
        File fileOnTop = Utils.join(GITLET_DIR, "HEAD.txt");
        String head = Utils.readContentsAsString(fileOnTop);

        File directoryBranches = Utils.join(GITLET_DIR, "branches");
        HashMap mapOfBranches = Utils.readObject(directoryBranches, HashMap.class);

        if (mapOfBranches.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
        } else {
            mapOfBranches.put(branchName, head);
            Utils.writeObject(directoryBranches, mapOfBranches);
        }
    }


    public static void find(String Message) {
        helperFind1(Message);
    }

    public static void helperFind1(String Message){
        ArrayList commits = Utils.readObject(Utils.join(GITLET_DIR, "commits"), ArrayList.class);
        Iterator<String> IteratorCommits = commits.iterator();
        boolean indicator = false;
        while (IteratorCommits.hasNext()) {
            String hashNextCommit = IteratorCommits.next();
            File fileOfNextCommit = Utils.join(GITLET_DIR, hashNextCommit);
            Commit nextCommit = Utils.readObject(fileOfNextCommit, Commit.class);
            if (nextCommit.getMessage().equals(Message)) {
                System.out.println(hashNextCommit);
                indicator = true;
            }
        }
        if (indicator == false) {
            System.out.println("Found no commit with that message.");
        }
    }


    public static void rm_branch(String branchName) {
        File branchDir = Utils.join(GITLET_DIR, "branches");
        HashMap branchesMap =
                Utils.readObject(branchDir, HashMap.class);
        File currentBranchFile =
                Utils.join(GITLET_DIR, "currentBranchName.txt");
        String currentBranchName =
                Utils.readContentsAsString(currentBranchFile);

        helperRmBranch(currentBranchName,  branchName,
                branchDir,  branchesMap);
    }

    public static void helperRmBranch(String currentBranchName,
         String branchName, File branchDir, HashMap branchesMap){
        if (!branchesMap.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else if (currentBranchName.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        } else {
            branchesMap.remove(branchName);
            Utils.writeObject(branchDir, branchesMap);
        }
    }

    public static void status() {
        helperStatus1();
        helperStatus2();
    }



    public static void helperStatus2() {
        System.out.println("=== Staged Files ===");
        File fileAdding = Utils.join(GITLET_DIR, "stagedForAddition");
        HashMap stagedForAddition = Utils.readObject(fileAdding, HashMap.class);
        Iterator<String> additionIterator = stagedForAddition.keySet().iterator();
        ArrayList<String> listAddition = new ArrayList<>();
        while (additionIterator.hasNext()) {
            listAddition.add(additionIterator.next());
        }
        Collections.sort(listAddition);
        Iterator<String> ListAddingIter = listAddition.iterator();
        while (ListAddingIter.hasNext()) {
            System.out.println(ListAddingIter.next());
        }
        System.out.println("");


        System.out.println("=== Removed Files ===");
        File fileRemoving = Utils.join(GITLET_DIR, "stagedForRemoval");
        HashMap stagedForRemoval = Utils.readObject(fileRemoving, HashMap.class);
        Iterator<String> removalIterator =
          stagedForRemoval.keySet().iterator();
        ArrayList<String> listRemoval = new ArrayList<>();
        while (removalIterator.hasNext()) {
            listRemoval.add(removalIterator.next());
        }
        Collections.sort(listRemoval);
        Iterator<String> listRemovingIter = listRemoval.iterator();
        while (listRemovingIter.hasNext()) {
            System.out.println(listRemovingIter.next());
        }
        System.out.println("");



        System.out.println("=== Modifications Not Staged For Commit ===");
        File headFile = Utils.join(GITLET_DIR, "HEAD.txt");
        String head = Utils.readContentsAsString(headFile);
        Commit commitCurrently = Utils.readObject(
          Utils.join(GITLET_DIR, head), Commit.class);
        List<String> directoryFiles = Utils.plainFilenamesIn(CWD);
        ArrayList<String> listModifiedVer = new ArrayList<>();
        ArrayList<String> listDeletedVer = new ArrayList<>();
        ArrayList<String> listMandatory = new ArrayList<>();
        Iterator<String> commitCurrentlyIter =
            commitCurrently.getFileBlob().keySet().iterator();
        while (commitCurrentlyIter.hasNext()) {
            String fileToBeTracked = commitCurrentlyIter.next();
            if (directoryFiles.contains(fileToBeTracked)) {
                File trackedFileCwd = Utils.join(CWD, fileToBeTracked);
                byte[] trackingCwd = Utils.readContents(trackedFileCwd);
                String hashCwdTracked = Utils.sha1(trackingCwd, "blob");
                if (!hashCwdTracked.equals(commitCurrently.getFileBlob().get(fileToBeTracked))
                   && !stagedForAddition.containsKey(fileToBeTracked)) {
                    if (!listMandatory.contains(fileToBeTracked)) {
                        listMandatory.add(fileToBeTracked);
                        listModifiedVer.add(fileToBeTracked);
                    }
                }
            } else {
                if (!stagedForRemoval.containsKey(fileToBeTracked)) {
                    if (!listMandatory.contains(fileToBeTracked)) {
                        listMandatory.add(fileToBeTracked);
                        listDeletedVer.add(fileToBeTracked);
                    }
                }
            }
        }

        Iterator<String> AddingIterator = stagedForAddition.keySet().iterator();
        while (AddingIterator.hasNext()) {
            String fileToBeStagedNext = AddingIterator.next();
            if (directoryFiles.contains(fileToBeStagedNext)) {
                File additionCwd = Utils.join(CWD, fileToBeStagedNext);
                byte[] addedFileCwd = Utils.readContents(additionCwd);
                String hashCwdAdded = Utils.sha1(addedFileCwd, "blob");
                if (!hashCwdAdded.equals(
                 stagedForAddition.get(fileToBeStagedNext))) {
                    if (!listMandatory.contains(fileToBeStagedNext)) {
                        listMandatory.add(fileToBeStagedNext);
                        listModifiedVer.add(fileToBeStagedNext);
                    }
                }
            } else {
                if (!listMandatory.contains(fileToBeStagedNext)) {
                    listMandatory.add(fileToBeStagedNext);
                    listDeletedVer.add(fileToBeStagedNext);
                }
            }
        }

        Collections.sort(listMandatory);
        Iterator<String> newIter = listMandatory.iterator();
        while (newIter.hasNext()) {
            String bothFilesNew = newIter.next();
            if (listModifiedVer.contains(bothFilesNew)) {
                System.out.println(bothFilesNew + " (modified)");
            } else if (listDeletedVer.add(bothFilesNew)) {
                System.out.println(bothFilesNew + " (deleted)");
            }
        }
        System.out.println("");


        helperStatus3(directoryFiles, stagedForAddition,  commitCurrently);
    }

    public static void helperStatus3(List<String> directoryFiles,
          HashMap stagedForAddition, Commit commitCurrently) {
        System.out.println("=== Untracked Files ===");
        Iterator<String> IteratorCwd = directoryFiles.iterator();
        ArrayList<String> listOfUntrackedFiles = new ArrayList<>();
        while (IteratorCwd.hasNext()) {
            String cwdNextFile = IteratorCwd.next();
            if (!stagedForAddition.containsKey(cwdNextFile)
             && !commitCurrently.getFileBlob().containsKey(cwdNextFile)) {
                listOfUntrackedFiles.add(cwdNextFile);
            }
        }
        Collections.sort(listOfUntrackedFiles);
        Iterator<String> notTrackedIterator = listOfUntrackedFiles.iterator();
        while (notTrackedIterator.hasNext()) {
            System.out.println(notTrackedIterator.next());
        }
        System.out.println("");
    }

    public static void helperStatus1(){
        File directoryBranches =
                Utils.join(GITLET_DIR, "branches");
        HashMap mapOfBranches =
                Utils.readObject(directoryBranches, HashMap.class);
        File fileBranchCurrently =
                Utils.join(GITLET_DIR, "currentBranchName.txt");
        String branchNameCurrently =
                Utils.readContentsAsString(fileBranchCurrently);


        System.out.println("=== Branches ===");
        Iterator<String> IteratorBranch = mapOfBranches.keySet().iterator();
        ArrayList<String> listOfBranch = new ArrayList<>();
        while (IteratorBranch.hasNext()) {
            listOfBranch.add(IteratorBranch.next());
        }
        Collections.sort(listOfBranch);
        Iterator<String> listOfBranchesIterator = listOfBranch.iterator();
        while (listOfBranchesIterator.hasNext()) {
            String branchNextTo = listOfBranchesIterator.next();
            if (branchNextTo.equals(branchNameCurrently)) {
                System.out.println("*" + branchNextTo);
            } else {
                System.out.println(branchNextTo);
            }
        }
        System.out.println("");
    }

    public static void reset(String commitID) {
        ArrayList allCommits = Utils.readObject(
         Utils.join(GITLET_DIR, "commits"), ArrayList.class);
        Iterator<String> iteratorCommits = allCommits.iterator();
        int outcome = commitID.length();
        if (outcome < 40) {
            while (iteratorCommits.hasNext()) {
                String nextFile = iteratorCommits.next();
                 if (nextFile.length() >= outcome
                 && nextFile.substring(0, outcome).equals(commitID)) {
                    commitID = nextFile;
                }
            }
        }
        resetHelper1(commitID, allCommits);
    }

    public static void resetHelper1(String commitID, ArrayList allCommits){
        if (!allCommits.contains(commitID)) {
            System.out.println("No commit with that id exists.");
        } else {
            List<String> filesInDirectory = Utils.plainFilenamesIn(CWD);
            Commit resetCommit = Utils.readObject(
                    Utils.join(GITLET_DIR, commitID), Commit.class);
            File headFile = Utils.join(GITLET_DIR, "HEAD.txt");
            String head = Utils.readContentsAsString(headFile);
            Commit currentCommit = Utils.readObject(
                    Utils.join(GITLET_DIR, head), Commit.class);

            Iterator<String> cwdIter = filesInDirectory.iterator();
            while (cwdIter.hasNext()) {
                String nextCwdFile = cwdIter.next();
                if (!currentCommit.getFileBlob().containsKey(nextCwdFile)
                        && resetCommit.getFileBlob().containsKey(nextCwdFile)) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    System.exit(0);
                } else if (currentCommit.getFileBlob().containsKey(nextCwdFile)
                  && !resetCommit.getFileBlob().containsKey(nextCwdFile)) {
                    Utils.restrictedDelete(Utils.join(CWD, nextCwdFile));
                }
            }
            Iterator<String> resetCommitIter =
                    resetCommit.getFileBlob().keySet().iterator();
            while (resetCommitIter.hasNext()) {
                checkoutWithCommitID(commitID, resetCommitIter.next());
            }

            File branchesDir =
                    Utils.join(GITLET_DIR, "branches");
            HashMap branchMap =
                    Utils.readObject(branchesDir, HashMap.class);
            File currentBranchNameFile =
                    Utils.join(GITLET_DIR, "currentBranchName.txt");
            String currentBranchName =
                    Utils.readContentsAsString(currentBranchNameFile);
            branchMap.put(currentBranchName, commitID);
            Utils.writeObject(branchesDir, branchMap);
            Utils.writeContents(headFile, commitID);

            File stagedForAdditionFile =
                    Utils.join(GITLET_DIR, "stagedForAddition");
            HashMap stagedForAddition =
                    Utils.readObject(stagedForAdditionFile, HashMap.class);
            File stagedForRemovalFile =
                    Utils.join(GITLET_DIR, "stagedForRemoval");
            HashMap stagedForRemoval =
                    Utils.readObject(stagedForRemovalFile, HashMap.class);
            stagedForAddition.clear();
            stagedForRemoval.clear();
            Utils.writeObject(stagedForAdditionFile, stagedForAddition);
            Utils.writeObject(stagedForRemovalFile, stagedForRemoval);
        }
    }












    public static void merge(String branchName) {

    }

}
