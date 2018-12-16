package pl.net.madejski;

import java.sql.ResultSet;

public class Employee {
    private Integer id;
    private String imie;
    private String nazwisko;
    private String kraj;
    private Integer placa;

    public Employee(Integer id, String imie, String nazwisko, String kraj, Integer placa) {
        this.id = id;
        this.imie = imie;
        this.nazwisko = nazwisko;
        this.kraj = kraj;
        this.placa = placa;
    }

    public static Employee fromResultSet(ResultSet resultSet) {
        try {
            Employee employee = new Employee(
                    resultSet.getInt("id"),
                    resultSet.getString("imie"),
                    resultSet.getString("nazwisko"),
                    resultSet.getString("kraj"),
                    resultSet.getInt("placa")
            );
            return employee;
        } catch (Exception ignored) { return null; }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getImie() {
        return imie;
    }

    public void setImie(String imie) {
        this.imie = imie;
    }

    public String getNazwisko() {
        return nazwisko;
    }

    public void setNazwisko(String nazwisko) {
        this.nazwisko = nazwisko;
    }

    public String getKraj() {
        return kraj;
    }

    public void setKraj(String kraj) {
        this.kraj = kraj;
    }

    public Integer getPlaca() {
        return placa;
    }

    public void setPlaca(Integer placa) {
        this.placa = placa;
    }

    @Override
    public String toString() {
        return id + "\t | " + imie + "\t | " + nazwisko + "\t | " + kraj + "\t | " + placa;
    }
}
