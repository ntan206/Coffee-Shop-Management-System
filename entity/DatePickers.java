package entity;

import org.jdatepicker.JDatePicker;
import org.jdatepicker.UtilDateModel;

import java.util.Calendar;
import java.util.Date;

public class DatePickers {

    // Tương thích với jar JDatePicker 2.0.1 mà constructor JDatePicker(JDatePanel) "not visible"
    public static JDatePicker createDatePicker() {
        UtilDateModel model = new UtilDateModel();

        Calendar cal = Calendar.getInstance();
        model.setDate(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        model.setSelected(true);

        // CHỈ dùng constructor public: JDatePicker(DateModel)
        JDatePicker picker = new JDatePicker(model);

        // optional: có thì dùng, không có thì thôi
        try { picker.setTextEditable(true); } catch (Throwable ignored) {}

        return picker;
    }

    public static Date getValue(JDatePicker picker) {
        if (picker == null) return null;

        Object v = picker.getModel().getValue();
        if (v == null) return null;

        if (v instanceof Date) return (Date) v;
        if (v instanceof Calendar) return ((Calendar) v).getTime();

        return null;
    }
}