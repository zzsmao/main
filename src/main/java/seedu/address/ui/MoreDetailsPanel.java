package seedu.address.ui;

import java.util.logging.Logger;

import com.google.common.eventbus.Subscribe;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.events.model.AddressBookChangedEvent;
import seedu.address.commons.events.ui.NewResultAvailableEvent;
import seedu.address.commons.events.ui.PersonPanelSelectionChangedEvent;
import seedu.address.model.assignment.Assignment;
import seedu.address.model.person.AssignmentStub;
import seedu.address.model.person.Person;

/**
 * The More Details Panel of the App.
 */
public class MoreDetailsPanel extends UiPart<Region> {

    private static final String FXML = "MoreDetailsPanel.fxml";
    private static final String DEFAULT_LABEL = "<No student selected>";
    private static final String DEFAULT_TEXT = "<No note found>";

    // Value that indicates that no student has been selected yet.
    private static final int NONE = -1;

    private final Logger logger = LogsCenter.getLogger(getClass());

    // Initializing test data
    private AssignmentStub[] assignments = {new AssignmentStub("Finals", 73), new AssignmentStub("Mid-terms", 39),
        new AssignmentStub("Participation", 10), new AssignmentStub("Product Demo", 101)};

    // List of students
    private ObservableList<Person> studentList;
    private ObservableList<Assignment> assignmentList;

    // Current student whose details are being shown
    private Person currentStudent = null;
    private int currentStudentIndex = NONE;

    @FXML
    private TextArea notesText;

    // Left side holds component name, eg. Participation, right side holds mark attained.
    @FXML
    private GridPane components;

    public MoreDetailsPanel(ObservableList<Person> listOfStudents, ObservableList<Assignment> listOfAssignments) {
        super(FXML);
        registerAsAnEventHandler(this);
        this.studentList = listOfStudents;
        this.assignmentList = listOfAssignments;

        // default label
        Label noComponents = new Label(DEFAULT_LABEL);
        noComponents.setFont(new Font("System", (double) 25));
        components.add(noComponents, 0, 0);

        // To prevent triggering events for typing inside the loaded Web page.
        getRoot().setOnKeyPressed(Event::consume);

    }

    @Subscribe
    private void handlePersonPanelSelectionChangedEvent(PersonPanelSelectionChangedEvent event) throws Exception {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        currentStudent = event.getNewSelection();
        currentStudentIndex = studentList.indexOf(currentStudent);
        // find student index
        display(currentStudentIndex); // display student's details in details panel
    }

    @Subscribe
    private void handleAddressbookChangedEvent(AddressBookChangedEvent event) throws Exception {
        // No student selected yet, don't need to display.
        if (currentStudentIndex == NONE) {
            return;
        }
        display(currentStudentIndex); // display student's details in details panel
    }

    @Subscribe
    private void handleSelectedStudentNoteChangeEvent(NewResultAvailableEvent event) throws Exception {
        // displays student again even if note added was to other students
        // prevents further knowledge of event by parsing event message to check for which student changed
        // No student selected yet, don't need to display.
        if (currentStudentIndex == NONE) {
            return;
        }
        display(currentStudentIndex); // display student's details in details panel
    }

    /**
     * Displays the student's details in the Details Panel on the bottom right.
     * Student obtained using his/her index to avoid displaying the wrong student when undo/redo executed.
     */
    public void display(int studentIndex) {
        Person student = null;
        try {
            student = studentList.get(studentIndex);
            // Keep track of who was just displayed
            currentStudent = student;
        } catch (IndexOutOfBoundsException e) {
            components.getChildren().clear();
            Label noComponents = new Label(DEFAULT_LABEL);
            noComponents.setFont(new Font("System", (double) 25));
            components.add(noComponents, 0, 0);
            notesText.clear();
            notesText.setText(DEFAULT_TEXT);
        }
        if (student == null) {
            return;
        }

        logger.info("Displaying details!\n");

        // remove old labels
        components.getChildren().clear();

        // add no. of rows equal to no. of assignments keyed in
        // Labels set to be label-bright
        Label label;
        String style = "-fx-font-size: 11pt;\n" + "-fx-font-family: \"Segoe UI Semibold\";\n"
            + "-fx-text-fill: white;\n" + "-fx-opacity: 1;";

        int row = 0;

        //Column headers
        label = new Label("Assignments");
        label.setStyle(style);
        components.add(label, 0, row);

        label = new Label("Deadline");
        label.setStyle(style);
        components.add(label, 1, row);

        label = new Label("Weight");
        label.setStyle(style);
        components.add(label, 2, row);

        label = new Label("Grade");
        label.setStyle(style);
        components.add(label, 3, row);

        double assignmentWeight;
        double assignmentMark;
        double assignmentMaxMark;
        double totalWeight = 0;
        double weightedMarks = 0;

        Assignment assignment;
        for (int i = 0; i < assignmentList.size(); i++) {
            assignment = assignmentList.get(i);
            row = i + 1;

            //Assignment index and name
            label = new Label(String.format("%d. %s", row, assignment.getName()));
            label.setStyle(style);
            components.add(label, 0, row);

            //Assignment deadline
            label = new Label(String.valueOf(assignment.getDeadline()));
            label.setStyle(style);
            components.add(label, 1, row);

            //Assignment weight
            assignmentWeight = assignment.getWeight().getValue();
            totalWeight += assignmentWeight;
            label = new Label(String.valueOf(assignment.getWeight()));
            label.setStyle(style);
            components.add(label, 2, row);

            //Assignment mark / maxMark
            if (student.getMarks().containsKey(assignment.getUniqueId())) {
                assignmentMark = student.getMarks().get(assignment.getUniqueId()).getValue();
                assignmentMaxMark = assignment.getMaxMark().getValue();

                label = new Label(String.format("%.1f/%.1f",
                    assignmentMark, assignmentMaxMark));

                assignmentMark /= assignmentMaxMark;
                weightedMarks += assignmentMark * assignmentWeight;
            } else {
                label = new Label("");
            }
            label.setStyle(style);
            components.add(label, 3, row);
        }

        row++;
        label = new Label("Total");
        label.setStyle(style);
        components.add(label, 0, row);

        //Total weightedMarks / totalWeight
        label = new Label(String.format("%.1f/%.1f",
            weightedMarks, totalWeight));
        label.setStyle(style);
        components.add(label, 3, row);

        // show student's notes
        notesText.clear();
        notesText.setText(student.getNote().toString());
    }

    public ObservableList<Person> getList() {
        return studentList;
    }

    public Person getCurrentStudent() {
        return currentStudent;
    }
}
