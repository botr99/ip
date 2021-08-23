import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Duke {
    private static final String HORIZONTAL_LINE = "____________________________________________________________";
    private static ArrayList<Task> tasks = new ArrayList<>();
    private static Parser parser;

    private static String formatDukeResponse(String response) {
        return HORIZONTAL_LINE + "\n" + response + "\n" + HORIZONTAL_LINE + "\n";
    }

    private static void printWelcomeMessage() {
        String logo = " ____        _        \n"
                + "|  _ \\ _   _| | _____ \n"
                + "| | | | | | | |/ / _ \\\n"
                + "| |_| | |_| |   <  __/\n"
                + "|____/ \\__,_|_|\\_\\___|\n";
        String welcomeMessage = logo + "Hello! I'm Duke.\n" + "What can I do for you?";
        System.out.println(formatDukeResponse(welcomeMessage));
    }

    private static void printExitMessage() {
        String exitMessage = "Bye. Hope to see you again soon!";
        System.out.println(formatDukeResponse(exitMessage));
    }

    private static void addTask(String description) throws DukeException, IOException {
        if (description == null || description.equals("")) {
            throw new DukeException("Oops!!! The description of a todo cannot be empty.");
        }

        Task task = new Todo(description);
        tasks.add(task);
        parser.addLine(task);
        printAddTaskMessage(task);
    }

    private static void addTask(String descriptionAndTime, String command) throws DukeException, IOException {
        String[] splitDescriptionAndTime;
        Task task;

        try {
            if (command.equals("deadline")) {
                splitDescriptionAndTime = descriptionAndTime.split(" /by ");
                task = new Deadline(splitDescriptionAndTime[0].trim(), splitDescriptionAndTime[1].trim());
            } else if (command.equals("event")) {
                splitDescriptionAndTime = descriptionAndTime.split(" /at ");
                task = new Event(splitDescriptionAndTime[0].trim(), splitDescriptionAndTime[1].trim());
            } else {
                printInvalidCommandMessage();
                return;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new DukeException("Oops!!! Deadlines or events should contain a description, followed by " +
                    "a /by or /at respectively, followed by a date or a time.");
        }

        tasks.add(task);
        parser.addLine(task);
        printAddTaskMessage(task);
    }

    private static void markTask(String taskNumberString) throws DukeException, IOException {
        int taskNumber = retrieveTaskNumber(taskNumberString);

        Task task = tasks.get(taskNumber - 1);
        task.markAsDone();
        parser.editLine(taskNumber, task);
        printMarkTaskDoneMessage(task);
    }

    private static void deleteTask(String taskNumberString) throws DukeException, IOException {
        int taskNumber = retrieveTaskNumber(taskNumberString);

        Task removedTask = tasks.remove(taskNumber - 1);
        parser.removeLine(removedTask);
        printDeleteTaskMessage(removedTask);
    }

    private static int retrieveTaskNumber(String taskNumberString) throws DukeException {
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(taskNumberString);
        } catch (NumberFormatException e) {
            throw new DukeException("Oops!!! The done or delete command should be followed by an integer.");
        }

        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new DukeException("Oops!!! The task number provided is not valid.");
        }
        return taskNumber;
    }

    private static void printAddTaskMessage(Task task) {
        System.out.println(formatDukeResponse("Got it. I've added this task:\n" + task
                + "\nNow you have " + tasks.size()
                + (tasks.size() == 1 ? " task " : " tasks ") + "in the list."));
    }

    private static void printTasksMessage() {
        StringBuilder tasksMessage = new StringBuilder("Here are the tasks in your list:");

        for (int i = 0; i < tasks.size(); i++) {
            int taskNumber = i + 1;
            tasksMessage.append("\n").append(taskNumber).append(".").append(tasks.get(i));
        }

        System.out.println(formatDukeResponse(tasksMessage.toString()));
    }

    private static void printMarkTaskDoneMessage(Task task) {
        System.out.println(formatDukeResponse("Nice! I've marked this task as done:\n" + task));
    }

    private static void printDeleteTaskMessage(Task task) {
        System.out.println(formatDukeResponse("Noted. I've removed this task:\n" + task
                + "\nNow you have " + tasks.size()
                + (tasks.size() == 1 ? " task " : " tasks ") + "in the list."));
    }

    private static void printInvalidCommandMessage() {
        System.out.println(formatDukeResponse("Oops!!! I'm sorry, but I don't know what that means."));
    }

    private static void printDukeExceptionMessage(DukeException e) {
        System.out.println(formatDukeResponse(e.getMessage()));
    }

    private static void scanUserInput() throws IOException {
        Scanner sc = new Scanner(System.in);
        while (true) {
            String[] userInput = sc.nextLine().trim().split(" ", 2);
            String command = userInput[0];
            String action = userInput.length == 2 ? userInput[1].trim() : "";

            if (command.equals("bye")) {
                break;
            }

            try {
                switch (command) {
                case "list":
                    printTasksMessage();
                    break;
                case "done":
                    markTask(action);
                    break;
                case "delete":
                    deleteTask(action);
                    break;
                case "todo":
                    addTask(action);
                    break;
                case "deadline":
                case "event":
                    addTask(action, command);
                    break;
                default:
                    printInvalidCommandMessage();
                    break;
                }
            } catch (DukeException e) {
                printDukeExceptionMessage(e);
            }
        }
        sc.close();
    }

    public static void main(String[] args) {
        try {
            Storage storage = new Storage("data/duke.txt");
            parser = new Parser(storage);
            tasks = parser.retrieveTaskListFromLines();

            printWelcomeMessage();
            scanUserInput();
            printExitMessage();

        } catch (IOException e) {
            System.out.println("Something went wrong. Please restart the app.");
        }
    }
}
