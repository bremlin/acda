package acda;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.stage.FileChooser;
import model.data.DataHelper;
import model.data.ItemType;
import model.data.TreeTableElement;
import model.sql.EmailSender;
import model.sql.SQLController;
import model.xml.XMLHelper;

import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    public ComboBox groupOne;
    @FXML
    public ComboBox groupTwo;
    @FXML
    public ComboBox groupThree;

    @FXML
    public Label currentDate;
    @FXML
    public ComboBox filterOneType;
    @FXML
    public ComboBox filterOneValue;

    @FXML
    TreeTableView<TreeTableElement> treeTableView;
    @FXML
    TreeTableColumn<TreeTableElement, String> idColumn;
    @FXML
    public TreeTableColumn<TreeTableElement, String> nameColumn;
    @FXML
    public TreeTableColumn<TreeTableElement, String> startDateColumn;
    @FXML
    public TreeTableColumn<TreeTableElement, String> finishDateColumn;
    @FXML
    public TreeTableColumn<TreeTableElement, String> pvTypeColumn;
    @FXML
    public TreeTableColumn<TreeTableElement, String> pvVolume;
    @FXML
    public TreeTableColumn<TreeTableElement, String> weight;
    @FXML
    public TreeTableColumn<TreeTableElement, String> percentComplete;

    private XMLHelper xmlHelper;
    private DataHelper dataHelper = new DataHelper();
    private DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    private SQLController sqlController;

    public void loadProject() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Загрузить XML-файл проекта");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XML", "*.xml"));
        File xmlFile = fileChooser.showOpenDialog(null);

        if (xmlFile != null) {
            xmlHelper = new XMLHelper(xmlFile);
            sqlController = new SQLController(xmlHelper.getProjectId());
            xmlHelper.fillElements(sqlController);
            dataHelper.setData(xmlHelper);
            treeTableView.setRoot(dataHelper.getRoot());
            treeTableView.setEditable(true);
            columnSettings();

            setGroupData();
        }
    }

    private void fillGroupData(String groupOneName, String groupNameTwo, String groupNameThree) {
        System.out.println("fillGroupData: " + groupOneName);
        treeTableView.setRoot(dataHelper.getGroupDataRoot(groupOneName, groupNameTwo, groupNameThree));
    }

    private void setGroupData() {
        if (groupOne.getItems().size() > 0) {
            groupOne.getItems().clear();
            filterOneType.getItems().clear();
        }
        groupOne.getItems().addAll(dataHelper.getGroupList());
        filterOneType.getItems().addAll(dataHelper.getGroupList());
    }

    private void columnSettings() {
        idColumn.setCellValueFactory(param -> param.getValue().getValue().getId());
        nameColumn.setCellValueFactory(param -> param.getValue().getValue().getName());
        startDateColumn.setCellValueFactory(param -> param.getValue().getValue().getStartDate());
        finishDateColumn.setCellValueFactory(param -> param.getValue().getValue().getFinishDate());
        pvTypeColumn.setCellValueFactory(param -> param.getValue().getValue().getPVType());
        pvVolume.setCellValueFactory(param -> param.getValue().getValue().getPvVolume());
        weight.setCellValueFactory(param -> param.getValue().getValue().getWeight());
        percentComplete.setCellValueFactory(param -> param.getValue().getValue().getPercent());

        percentComplete.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        percentComplete.setOnEditCommit(event -> {
            TreeItem<TreeTableElement> currentEditStep = treeTableView.getTreeItem(event.getTreeTablePosition().getRow());
            if (currentEditStep.getValue().getElementType() == ItemType.Step) {
                currentEditStep.getValue().setPercent(event.getNewValue());
                sqlController.insertPercentComplete(currentEditStep.getValue().getObjectId(), event.getNewValue());
            }
        });

        treeTableView.setRowFactory(tv -> new TreeTableRow<TreeTableElement>() {
            @Override
            public void updateItem(TreeTableElement item, boolean empty) {
                super.updateItem(item, empty) ;
                if (item == null) {
                    addStyles(null, getStyleClass());
                } else {
                    addStyles(item.getElementType(), getStyleClass());
                }
            }
        });
    }

    private void addStyles(ItemType type, ObservableList<String> styleClass) {
        styleClass.clear();
        styleClass.add("cell");
        styleClass.add("indexed-cell");
        styleClass.add("tree-table-row-cell");
        if (type!= null) styleClass.add(type.getStyle());
    }

    public void save() {
        EmailSender emailSender = new EmailSender();
        emailSender.sendEmail(xmlHelper.getEmail(), sqlController.getDbPath());
    }

    public void groupOneAction() {
        if (groupTwo.getItems().size() > 0) groupTwo.getItems().clear();
        groupTwo.getItems().addAll(dataHelper.getGroupListTwo(String.valueOf(groupOne.getSelectionModel().getSelectedItem())));
    }

    public void groupTwoAction() {
        if (groupThree.getItems().size() > 0) groupThree.getItems().clear();
        groupThree.getItems().addAll(dataHelper.getGroupListThree(
                String.valueOf(groupOne.getSelectionModel().getSelectedItem()),
                String.valueOf(groupTwo.getSelectionModel().getSelectedItem())));
    }

    public void filter(ActionEvent actionEvent) {

    }

    public void apply() {
        System.out.println("apply");
        if (checkGroupAndFilter()) {
            System.out.println("checkGroupAndFilter: " + checkGroupAndFilter());
            String firstFilter = groupOne.getSelectionModel().getSelectedItem().toString();
            String secondFilter = null;
            String thirdFilter = null;
            if (groupTwo.getSelectionModel().getSelectedItem() != null) {
                secondFilter = groupTwo.getSelectionModel().getSelectedItem().toString();
            }
            if (groupThree.getSelectionModel().getSelectedItem() != null) {
                thirdFilter = groupThree.getSelectionModel().getSelectedItem().toString();
            }
            fillGroupData(firstFilter, secondFilter, thirdFilter);
        }
    }

    public void cancel() {
        groupOne.getSelectionModel().clearSelection();
        filterOneType.getSelectionModel().clearSelection();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentDate.setText("Текущая дата: " + df.format(Calendar.getInstance().getTime()));
    }

    private boolean checkGroupAndFilter() {
        return groupOne.getSelectionModel().getSelectedItem() != null;
    }
}
