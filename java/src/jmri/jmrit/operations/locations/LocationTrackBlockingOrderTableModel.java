// LocationTrackBlockingOrderTableModel.java
package jmri.jmrit.operations.locations;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import jmri.util.swing.XTableColumnModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table Model for edit of tracks used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2015
 * @version $Revision: 28746 $
 */
public class LocationTrackBlockingOrderTableModel extends AbstractTableModel implements PropertyChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = -7318273766197527079L;

    protected Location _location;
    protected List<Track> _tracksList = new ArrayList<Track>();
    protected JTable _table;

    // Defines the columns
    protected static final int ID_COLUMN = 0;
    protected static final int NAME_COLUMN = 1;
    protected static final int TYPE_COLUMN = 2;
    protected static final int ORDER_COLUMN = 3;

    protected static final int HIGHESTCOLUMN = ORDER_COLUMN + 1;

    public LocationTrackBlockingOrderTableModel() {
        super();
    }

    synchronized void updateList() {
        if (_location == null) {
            return;
        }
        // first, remove listeners from the individual objects
        removePropertyChangeTracks();

        _tracksList = _location.getTracksByBlockingOrderList(null);
        // and add them back in
        for (Track track : _tracksList) {
            track.addPropertyChangeListener(this);
        }
        fireTableDataChanged();
    }

    protected void initTable(JTable table, Location location) {
        _table = table;
        _location = location;
        if (_location != null) {
            _location.addPropertyChangeListener(this);
        }
        initTable();
        // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        updateList();
    }

    private void initTable() {
        // Use XTableColumnModel so we can control which columns are visible
        XTableColumnModel tcm = new XTableColumnModel();
        _table.setColumnModel(tcm);
        _table.createDefaultColumnsFromModel();

        // set column preferred widths
        tcm.getColumn(ID_COLUMN).setPreferredWidth(40);
        tcm.getColumn(NAME_COLUMN).setPreferredWidth(200);
        tcm.getColumn(TYPE_COLUMN).setPreferredWidth(80);
        tcm.getColumn(ORDER_COLUMN).setPreferredWidth(60);
    }

    public int getRowCount() {
        return _tracksList.size();
    }

    public int getColumnCount() {
        return HIGHESTCOLUMN;
    }

    public String getColumnName(int col) {
        switch (col) {
            case ID_COLUMN:
                return Bundle.getMessage("Id");
            case NAME_COLUMN:
                return Bundle.getMessage("TrackName");
            case TYPE_COLUMN:
                return Bundle.getMessage("Type");
            case ORDER_COLUMN:
                return Bundle.getMessage("ServiceOrder");
            default:
                return "unknown"; // NOI18N
        }
    }

    public Class<?> getColumnClass(int col) {
        switch (col) {
            case ID_COLUMN:
                return String.class;
            case NAME_COLUMN:
                return String.class;
            case TYPE_COLUMN:
                return String.class;
            case ORDER_COLUMN:
                return Integer.class;
            default:
                return null;
        }
    }

    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case ORDER_COLUMN:
                return true;
            default:
                return false;
        }
    }

    public Object getValueAt(int row, int col) {
        if (row >= _tracksList.size()) {
            return "ERROR row " + row; // NOI18N
        }
        Track track = _tracksList.get(row);
        if (track == null) {
            return "ERROR track unknown " + row; // NOI18N
        }
        switch (col) {
            case ID_COLUMN:
                return track.getId();
            case NAME_COLUMN:
                return track.getName();
            case TYPE_COLUMN:
                return track.getTrackTypeName();
            case ORDER_COLUMN:
                return track.getBlockingOrder();
            default:
                return "unknown " + col; // NOI18N
        }
    }

    public void setValueAt(Object value, int row, int col) {
        if (row >= _tracksList.size()) {
            return;
        }
        Track track = _tracksList.get(row);
        if (track == null) {
            return; // NOI18N
        }
        switch (col) {
            case ORDER_COLUMN:
                if ((int) value >= 0)
                    track.setBlockingOrder((int) value);
                break;
            default:
                break;
        }
    }

    // this table listens for changes to a location and it's tracks
    public void propertyChange(PropertyChangeEvent e) {
//        if (Control.showProperty && log.isDebugEnabled())
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        if (e.getPropertyName().equals(Location.TRACK_LISTLENGTH_CHANGED_PROPERTY)) {
            updateList();
        }
        if (e.getPropertyName().equals(Track.TRACK_BLOCKING_ORDER_CHANGED_PROPERTY)
                || e.getPropertyName().equals(Track.NAME_CHANGED_PROPERTY)
                || e.getPropertyName().equals(Track.TRACK_TYPE_CHANGED_PROPERTY)) {
            fireTableDataChanged();
        }
    }

    protected void removePropertyChangeTracks() {
        for (Track track : _tracksList) {
            track.removePropertyChangeListener(this);
        }
    }

    public void dispose() {
        // if (log.isDebugEnabled())
        // log.debug("dispose");
        removePropertyChangeTracks();
        if (_location != null) {
            _location.removePropertyChangeListener(this);
        }
        _tracksList.clear();
        fireTableDataChanged();
    }

    static Logger log = LoggerFactory.getLogger(LocationTrackBlockingOrderTableModel.class.getName());
}
