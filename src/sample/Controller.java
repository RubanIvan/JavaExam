package sample;

import com.mongodb.*;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

public class Controller {


    @FXML
    DatePicker datePicker;

    @FXML
    TextField TextTitle;

    @FXML
    ListView TaskList;

    @FXML
    TextArea   TextDesc;

    @FXML
    Button BtnCreate;

    @FXML
    Button BtnEdit;

    @FXML
    Button BtnDel;

    //ссылка на таблицу
    private DBCollection DBTable;

    public void initialize()
    {
        datePicker.setValue(LocalDate.now());


            MongoClient mongo = new MongoClient("localhost");
            DB db = mongo.getDB("local");
            DBTable = db.getCollection("Task");

        TaskListUpdate();

        //обработчик события onClick на списке
        TaskList.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<TaskTitle>() {
                    @Override
                    public void changed(ObservableValue<? extends TaskTitle> observable, TaskTitle oldValue, TaskTitle newValue) {
                        //System.out.println("Selected item: " + newValue);
                        TaskShow(newValue._id);

                    }
                });

        //обработчик события onSelect в календаре
        datePicker.setOnAction(event -> {
            LocalDate date = datePicker.getValue();
            //System.out.println("Selected date: " + date);
            TaskListUpdate();
            TaskClear();
        });

        //раскрашиваем дни с тасками
        final Callback<DatePicker, DateCell> dayCellFactory =
                new Callback<DatePicker, DateCell>() {
                    @Override
                    public DateCell call(final DatePicker datePicker) {
                        return new DateCell() {
                            @Override
                            public void updateItem(LocalDate item, boolean empty) {
                                super.updateItem(item, empty);

                                DBObject query = new BasicDBObject("Date",item.toString());
                                DBCursor Cursor = DBTable.find(query);

                                if(Cursor.length()>0  ){
                                    //setDisable(true);
                                    setStyle("-fx-background-color: #a4ff59;");
                                }
                            }
                        };
                    }
                };
        datePicker.setDayCellFactory(dayCellFactory);



    }

    public void TaskShow(String id)
    {
        DBObject query = new BasicDBObject("_id",new ObjectId(id));
        DBObject o = DBTable.findOne(query);
        TextTitle.setText(o.get("Title").toString());
        TextDesc.setText(o.get("Desc").toString());
    }


    //достатьл из базы заголовки и воткнуть их в список
    public void TaskListUpdate()
    {
        DBObject query = new BasicDBObject("Date",datePicker.getValue().toString());
        DBCursor Cursor = DBTable.find(query);

        ObservableList<TaskTitle> item= FXCollections.observableArrayList();

        while (Cursor.hasNext())
        {
            DBObject o=Cursor.next();
            item.add(new TaskTitle(o.get("_id").toString(),o.get("Title").toString()));
        }

        TaskList.setItems(item);
    }

    //Создать напоминание
    public void BtnCreate()
    {
        //Если нажали создать очищаем форму ввода активирует редактирование
        if(BtnCreate.getText().equals("Создать"))
        {
            BtnEdit.setDisable(true);
            BtnDel.setDisable(true);

            BtnCreate.setText("Сохранить");
            TextTitle.setEditable(true);
            TextDesc.setEditable(true);
            TaskClear();
            return;
        }

        if(BtnCreate.getText().equals("Сохранить"))
        {
            //в полях должен быть какойто текст
            if(TextDesc.getText().trim().length()==0 || TextTitle.getText().trim().length()==0 )
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Все поля должны быть заполнены");
                alert.showAndWait();
                return;
            }

            DBObject query = new BasicDBObject();
            query.put("Title",TextTitle.getText());
            query.put("Desc",TextDesc.getText());
            query.put("Date",datePicker.getValue().toString());
            DBTable.insert(query);

            BtnCreate.setText("Создать");
            TextTitle.setEditable(false);
            TextDesc.setEditable(false);
            TaskListUpdate();

            BtnEdit.setDisable(false);
            BtnDel.setDisable(false);
        }

    }

    //Кнопка редактировать
    public void BtnEdit()
    {
        if(BtnEdit.getText().equals("Изменить")) {
            BtnCreate.setDisable(true);
            BtnDel.setDisable(true);

            TextTitle.setEditable(true);
            TextDesc.setEditable(true);
            BtnEdit.setText("Сохранить");

            return;
        }

        if(BtnEdit.getText().equals("Сохранить")) {
            TextTitle.setEditable(false);
            TextDesc.setEditable(false);

            TaskTitle task= (TaskTitle) TaskList.getSelectionModel().getSelectedItem();

            DBObject query = new BasicDBObject();
            query.put("_id",new ObjectId(task._id));

            DBObject newdata = new BasicDBObject();
            newdata.put("Title", TextTitle.getText());
            newdata.put("Desc", TextDesc.getText());
            newdata.put("Date", datePicker.getValue().toString());
            DBTable.update(query,newdata);

            BtnEdit.setText("Изменить");

            BtnCreate.setDisable(false);
            BtnDel.setDisable(false);

        }

    }

    //Кнопка удалить
    public void BtnDel()
    {
        TaskTitle task= (TaskTitle) TaskList.getSelectionModel().getSelectedItem();
        if(task == null ) return;

        DBObject query = new BasicDBObject();
        query.put("_id",new ObjectId(task._id));
        DBTable.remove(query);

        TaskClear();
        TaskListUpdate();
    }


    public void TaskClear()
    {
        TextTitle.setText("");
        TextDesc.setText("");
    }

}
