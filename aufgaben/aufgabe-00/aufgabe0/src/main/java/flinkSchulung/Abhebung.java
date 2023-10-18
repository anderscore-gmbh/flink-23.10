package flinkSchulung;

public class Abhebung {

    private long automat_id;
    private String person_name;
    private int betrag_abgehoben;

    public Abhebung() {
    }

    public Abhebung(long automat_id, String person_name, int betrag_abgehoben) {
        this.automat_id = automat_id;
        this.person_name = person_name;
        this.betrag_abgehoben = betrag_abgehoben;
    }

    public long getAutomat_id() {
        return automat_id;
    }

    public void setAutomat_id(long automat_id) {
        this.automat_id = automat_id;
    }

    public String getPerson_name() {
        return person_name;
    }

    public void setPerson_name(String person_name) {
        this.person_name = person_name;
    }

    public int getBetrag_abgehoben() {
        return betrag_abgehoben;
    }

    public void setBetrag_abgehoben(int betrag_abgehoben) {
        this.betrag_abgehoben = betrag_abgehoben;
    }

    @Override
    public String toString() {
        return "Abhebung{" +
                "automat_id=" + automat_id +
                ", person_name='" + person_name + '\'' +
                ", betrag_abgehoben=" + betrag_abgehoben +
                '}';
    }
}
