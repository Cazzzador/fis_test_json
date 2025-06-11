import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;

public class Main {

    public static boolean isClientApproved(String filePath) {
        try {
            Gson gson = new Gson();
            JsonObject client = gson.fromJson(new FileReader(filePath), JsonObject.class);

            // Проверка 1. Минимальный возраст
            LocalDate birthDate = LocalDate.parse(client.get("birthDate").getAsString().substring(0, 10));
            int age = Period.between(birthDate, LocalDate.now()).getYears();
            if (age < 20) {
                return false;
            }

            // Проверка 2. Проверка действительности паспорта.
            JsonObject passport = client.getAsJsonObject("passport");
            LocalDate issuedAt = LocalDate.parse(passport.get("issuedAt").getAsString().substring(0, 10));
            if ((age > 20 && issuedAt.isBefore(birthDate.plusYears(20))) ||
                    (age > 45 && issuedAt.isBefore(birthDate.plusYears(45)))) {
                return false;
            }

            // Проверка 3. Полвеока кредитной истории
            JsonArray credits = client.getAsJsonArray("creditHistory");
            int countOverdueMore15Days = 0;

            for (JsonElement elem : credits) {
                JsonObject credit = elem.getAsJsonObject();
                String type = credit.get("type").getAsString();
                double overdue = credit.get("currentOverdueDebt").getAsDouble();
                int daysOverdue = credit.get("numberOfDaysOnOverdue").getAsInt();

                if (type.equals("Кредитная карта")) {
                    if (overdue > 0) {
                        return false;
                    }
                    if (daysOverdue > 30) {
                        return false;
                    }
                } else {
                    if (overdue > 0) {
                        return false;
                    }
                    if (daysOverdue > 60) {
                        return false;
                    }
                    if (daysOverdue > 15) {
                        countOverdueMore15Days++;
                        if (countOverdueMore15Days > 2) {
                            return false;
                        }
                    }
                }
            }

            return true;

        } catch (Exception e) {
            System.out.println("Название файла неправильное");
            return false;
        }
    }

    public static void main(String[] args) {
        for (int i = 1; i <= 4; i++) {
            boolean result = isClientApproved("src/test"+ i +".json");
            System.out.println("Результат "+ i + " теста " +result);
        }


    }
}
