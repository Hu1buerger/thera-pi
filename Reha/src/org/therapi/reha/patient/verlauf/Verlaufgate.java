package org.therapi.reha.patient.verlauf;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sql.DatenquellenFactory;

class Verlaufgate {
    private static final String INSERT_PREFIX = "INSERT INTO verlauf ( `patient_id`,  `therapist`,  `documentator`, `rezept_nr` , `documentedday`,  `dayofdocumentation`, `text`)";
    DatenquellenFactory ds;
    private Logger logger = LoggerFactory.getLogger(Verlaufgate.class);

    public Verlaufgate(DatenquellenFactory ds2) {
        ds = ds2;
    }

    Optional<Verlauf> find(int id) {
        String findSQL = "Select * from verlauf WHERE id = ";

        try (Statement statement = ds.createConnection()
                                     .createStatement();
                ResultSet rs = statement.executeQuery(findSQL + id);) {
            Verlauf result = null;

            if (rs.next()) {
                result = new Verlauf();
                result.id = rs.getInt("id");
                result.patientID = rs.getInt("patient_id");
                result.therapist = rs.getString("therapist");
                result.documentator = rs.getString("documentator");
                result.rezeptNr =rs.getString("rezept_nr");
                result.documentedDay = rs.getDate("documentedday")
                                         .toLocalDate();
                result.dayofDocumentation = rs.getDate("dayofdocumentation")
                                              .toLocalDate();
                result.text = rs.getNString("text");

            }

            return Optional.ofNullable(result);
        } catch (SQLException e) {
            logger.error("trying to find Verlauf ID " + id, e);
        }

        return Optional.<Verlauf>empty();
    }

    public int insert(Verlauf verlauf) {

        String SQL = INSERT_PREFIX + " VALUES " + sqlValuesExpression(verlauf) + ";";
        int key = 0;
        try {
            Statement statement = ds.createConnection()
                                    .createStatement();
            statement.execute(SQL, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = statement.getGeneratedKeys();
            key = rs.next() ? rs.getInt("id") : 0;
        } catch (SQLException e) {
            logger.error("cannot write " + verlauf + " to database ");
        }

        return key;
    }

    private void insert(List<Verlauf> inserts) {
        if (inserts == null || inserts.isEmpty()) {
            return;
        }
        String SQL = INSERT_PREFIX + " VALUES ";
        String[] strings = inserts.stream()
                                  .map(v -> sqlValuesExpression(v))
                                  .collect(Collectors.toList())
                                  .toArray(new String[] {});
        SQL += String.join(",", strings) + ";";
        try {
            Statement statement = ds.createConnection()
                                    .createStatement();
            statement.execute(SQL);

        } catch (SQLException e) {
            logger.error("cannot insert into  to database ", e);
        }

    }

    private String sqlValuesExpression(Verlauf verlauf) {
        return "(" + verlauf.patientID + ",\""
                   + verlauf.therapist + "\",\""
                   + verlauf.documentator  + "\",\""
                   + verlauf.rezeptNr+ "\",'"

                + java.sql.Date.valueOf(verlauf.documentedDay) + "','"
                + java.sql.Date.valueOf(verlauf.dayofDocumentation) + "',\"" + verlauf.text + "\")";
    };

    public Optional<Verlauf> verlauf(ResultSet rs) {
        Verlauf result = null;
        try {
            if (rs.next()) {
                result = new Verlauf();
                result.id = rs.getInt("id");
                result.patientID = rs.getInt("patient_id");
                result.therapist = rs.getString("therapist");
                result.documentator = rs.getString("documentator");
                result.documentedDay = rs.getDate("documentedday")
                                         .toLocalDate();
                result.dayofDocumentation = rs.getDate("dayofdocumentation")
                                              .toLocalDate();
                result.text = rs.getNString("text");

            }
        } catch (SQLException e) {
            logger.error("transforming resultset to Verlauf failed", e);
        }
        return Optional.ofNullable(result);
    }

    List<Verlauf> findByPatientId(int pat_id) {
        List<Verlauf> liste = new LinkedList<Verlauf>();
        String findSQL = "Select * from verlauf WHERE patient_Id = ";

        try (Statement statement = ds.createConnection()
                                     .createStatement();
                ResultSet rs = statement.executeQuery(findSQL + pat_id);) {

            while (rs.next()) {
                Verlauf result = new Verlauf();
                result.id = rs.getInt("id");
                result.patientID = rs.getInt("patient_id");
                result.therapist = rs.getString("therapist");
                result.documentator = rs.getString("documentator");
                result.documentedDay = rs.getDate("documentedday")
                                         .toLocalDate();
                result.dayofDocumentation = rs.getDate("dayofdocumentation")
                                              .toLocalDate();
                result.text = rs.getNString("text");

                liste.add(result);

            }
        } catch (SQLException e) {
            logger.error("trying to find Patient ID " + pat_id, e);
        }
        return liste;
    }

    public int update(List<Verlauf> verlaeufe) {
        if (verlaeufe == null || verlaeufe.isEmpty())
            return 0;

        try (Statement statement = ds.createConnection()
                                     .createStatement()) {
            for (Verlauf verlauf : verlaeufe) {
                statement.addBatch(createUpdateSQL(verlauf));
            }
            int[] numberOfLines = statement.executeBatch();
            return Arrays.stream(numberOfLines)
                         .sum();
        } catch (SQLException e) {
            logger.error("update verlaeufe in List " + verlaeufe, e);
        }
        return 0;

    }

    private String createUpdateSQL(Verlauf verlauf) {

        return "UPDATE `verlauf` SET `patient_id`='" + verlauf.patientID + "', `therapist`='"
                + Optional.ofNullable(verlauf.therapist)
                          .orElse("")
                + "', `documentator`='" + Optional.ofNullable(verlauf.documentator)
                                                  .orElse("")
                + "', `documentedday`='" + verlauf.documentedDay + "', `dayofdocumentation`='"
                + verlauf.dayofDocumentation + "' WHERE  `id`=" + verlauf.id + ";";
    }

    public void save(List<Verlauf> verlaeufe) {
        Map<Boolean, List<Verlauf>> result = verlaeufe.stream()
                                                      .collect(Collectors.partitioningBy(verlauf -> verlauf.id == 0));

        List<Verlauf> inserts = result.get(Boolean.TRUE)
                                      .stream()
                                      .filter(v -> !v.text.isEmpty())
                                      .collect(Collectors.toList());
        insert(inserts);
        List<Verlauf> updates = result.get(Boolean.FALSE);
        update(updates);

    }

}
