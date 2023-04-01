package gitlet;

/**
 *  @author Krish Rambhiya
 */
public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                checkArgValidity(args, 1);
                Repository.init();
                break;
            case "add":
                checkArgValidity(args, 2);
                Repository.add(args[1]);
                break;
            case "log":
                checkArgValidity(args, 1);
                Repository.log();
                break;
            case "commit":
                checkArgValidity(args, 2);
                Repository.commit(args[1]);
                break;
            case "global-log":
                Repository.initializedCheck();
                checkArgValidity(args, 1);
                Repository.global_log();
                break;
            case "reset":
                Repository.initializedCheck();
                checkArgValidity(args, 2);
                Repository.reset(args[1]);
                break;
            case "branch":
                Repository.initializedCheck();
                checkArgValidity(args, 2);
                Repository.branch(args[1]);
                break;
            case "rm":
                Repository.initializedCheck();
                checkArgValidity(args, 2);
                Repository.rm(args[1]);
                break;
            case "find":
                Repository.initializedCheck();
                checkArgValidity(args, 2);
                Repository.find(args[1]);
                break;
            case "checkout":
                Repository.initializedCheck();
                if (args.length == 3) {
                    if (!args[1].equals("--")) {
                        System.out.println("Incorrect operands");
                        System.exit(0);
                    }
                    Repository.checkout(args[2]);
                } else if (args.length == 4) {
                    if (!args[2].equals("--")) {
                        System.out.println("Incorrect operands");
                        System.exit(0);
                    }
                    Repository.checkoutWithCommitID(args[1], args[3]);
                } else {
                    checkArgValidity(args, 2);
                    Repository.checkoutBranch(args[1]);
                }
                break;
            case "status":
                Repository.initializedCheck();
                checkArgValidity(args, 1);
                Repository.status();
                break;
            case "rm-branch":
                Repository.initializedCheck();
                checkArgValidity(args, 2);
                Repository.rm_branch(args[1]);
                break;
            case "merge":
                Repository.initializedCheck();
                checkArgValidity(args, 2);
                Repository.merge(args[1]);
                break;
            default:
                System.out.println("command with the name inputted is non-existent.");
                System.exit(0);
        }
    }
    public static void checkArgValidity(String args[], int n) {
        if (args.length != n) {
            System.out.println("arguments passed invalid number.");
            System.exit(0);
        }
    }
}
