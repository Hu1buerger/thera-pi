package update;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import sql.DatenquellenFactory;

public class DueUpdates {

    private final DatenquellenFactory dq;

    public DueUpdates(DatenquellenFactory datenquellenFactory) {
        this.dq = datenquellenFactory;
    }
    private Queue<Update> updates = new ConcurrentLinkedQueue<>();

    protected void add(Update u) {
        updates.offer(u);
    }


    public void execute() {
      while (!updates.isEmpty()) {
          Update u = updates.poll();
          u.consider(dq);
      }
    }


    public void init() {

        add(new VerkaufsTabellenUpdate());
        add(new Add_Indischluessel_NF_QF_2_HMRCheck());

    }

}
