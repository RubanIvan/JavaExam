package sample;

/**
 * Created by AdminIvan on 25.11.2015.
 */
public class TaskTitle {

    public String _id;
    public String Title;

    @Override
    public String toString() {
        return Title;
    }

    TaskTitle(String id,String title ){
        _id=id;
        Title=title;
    }
}
