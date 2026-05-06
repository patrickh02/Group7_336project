package com.flightreservation.ui.customer;

import com.flightreservation.model.Customer;
import com.flightreservation.service.CustomerService;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.Date;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class FlightSearchPanel extends JPanel {

    private final CustomerService service;
    private final Customer        customer;

    private JTextField    depField;
    private JTextField    arrField;
    private JTextField    dateField;
    private JLabel        returnDateLabel;
    private JTextField    returnDateField;
    private JComboBox<String> tripTypeBox;
    private JCheckBox     flexibleCheck;

    private JComboBox<String> sortBox;
    private JComboBox<String> airlineFilterBox;
    private JTextField        maxPriceField;
    private JTextField        maxStopsField;
    private JTextField        depAfterField;
    private JTextField        arrBeforeField;

    private DefaultTableModel tableModel;
    private JTable            table;
    private JLabel            countLabel;

    private List<Object[]> rawResults     = new ArrayList<>();
    private List<Object[]> originalResults = new ArrayList<>();
    private List<String>   allAirlines = new ArrayList<>();

    private static final String[] COLS = {
        "Flight ID", "Flight#", "Airline", "From", "To",
        "Dep Time", "Arr Time", "Type", "Days", "Stops", "Economy$", "Business$", "First$", "Seats Left"
    };

    public FlightSearchPanel(CustomerService service, Customer customer) {
        this.service  = service;
        this.customer = customer;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        add(buildSearchForm(),  BorderLayout.NORTH);
        add(buildResultsArea(), BorderLayout.CENTER);
        add(buildBookButton(),  BorderLayout.SOUTH);
    }

    private JPanel buildSearchForm() {
        JPanel outer = new JPanel(new BorderLayout(0, 6));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        row1.add(new JLabel("From Airport (e.g. JFK):"));
        depField = new JTextField("JFK", 6);
        row1.add(depField);

        row1.add(new JLabel("To Airport (e.g. LAX):"));
        arrField = new JTextField("LAX", 6);
        row1.add(arrField);

        row1.add(new JLabel("Date (YYYY-MM-DD):"));
        dateField = new JTextField("2026-06-15", 11);
        row1.add(dateField);

        row1.add(new JLabel("Trip:"));
        tripTypeBox = new JComboBox<>(new String[]{"One-Way", "Round-Trip"});
        row1.add(tripTypeBox);

        returnDateLabel = new JLabel("Return Date:");
        returnDateLabel.setVisible(false);
        row1.add(returnDateLabel);
        returnDateField = new JTextField("2026-06-22", 11);
        returnDateField.setVisible(false);
        row1.add(returnDateField);

        flexibleCheck = new JCheckBox("Flexible ±3 days");
        row1.add(flexibleCheck);

        JButton searchBtn = new JButton("Search");
        searchBtn.setFont(searchBtn.getFont().deriveFont(Font.BOLD));
        row1.add(searchBtn);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        row2.add(new JLabel("Sort by:"));
        sortBox = new JComboBox<>(new String[]{
            "Departure Time", "Arrival Time", "Economy Price ↑", "Economy Price ↓",
            "Duration ↑", "Duration ↓"
        });
        row2.add(sortBox);

        row2.add(new JLabel("Airline:"));
        airlineFilterBox = new JComboBox<>(new String[]{"All"});
        row2.add(airlineFilterBox);

        row2.add(new JLabel("Max Price ($):"));
        maxPriceField = new JTextField("", 6);
        row2.add(maxPriceField);

        row2.add(new JLabel("Max Stops:"));
        maxStopsField = new JTextField("", 3);
        row2.add(maxStopsField);

        row2.add(new JLabel("Depart After (HH:MM):"));
        depAfterField = new JTextField("", 5);
        row2.add(depAfterField);

        row2.add(new JLabel("Arrive Before (HH:MM):"));
        arrBeforeField = new JTextField("", 5);
        row2.add(arrBeforeField);

        JButton applyBtn = new JButton("Apply Filters");
        row2.add(applyBtn);

        countLabel = new JLabel("  0 flight(s) found");
        row2.add(countLabel);

        outer.add(row1, BorderLayout.NORTH);
        outer.add(row2, BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> doSearch());
        dateField.addActionListener(e -> doSearch());
        applyBtn.addActionListener(e -> applyFiltersAndSort());
        sortBox.addActionListener(e -> applyFiltersAndSort());
        airlineFilterBox.addActionListener(e -> applyFiltersAndSort());
        tripTypeBox.addActionListener(e -> {
            boolean roundTrip = "Round-Trip".equals(tripTypeBox.getSelectedItem());
            returnDateLabel.setVisible(roundTrip);
            returnDateField.setVisible(roundTrip);
            revalidate();
        });

        return outer;
    }

    private JScrollPane buildResultsArea() {
        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {70, 65, 130, 55, 55, 75, 75, 90, 160, 50, 80, 80, 70, 80};
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                Component comp = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                int modelRow = t.convertRowIndexToModel(row);
                if (!sel && modelRow < rawResults.size()) {
                    Object[] r = rawResults.get(modelRow);
                    int capacity = (Integer) r[15];
                    int booked   = (Integer) r[16];
                    if (booked >= capacity) {
                        comp.setBackground(new Color(255, 230, 230));
                    } else {
                        comp.setBackground(Color.WHITE);
                    }
                }
                return comp;
            }
        });

        return new JScrollPane(table);
    }

    private JPanel buildBookButton() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        JButton bookBtn = new JButton("Book Selected Flight →");
        bookBtn.setFont(bookBtn.getFont().deriveFont(Font.BOLD, 13f));
        bookBtn.setBackground(Color.WHITE);
        bookBtn.setForeground(Color.BLACK);
        bookBtn.setOpaque(true);
        p.add(bookBtn);
        p.add(new JLabel("(Select a row above, then click Book)"));

        bookBtn.addActionListener(e -> openBookingDialog());
        return p;
    }

    // ── Search logic ──────────────────────────────────────────────────────────

    private void doSearch() {
        String dep = depField.getText().trim().toUpperCase();
        String arr = arrField.getText().trim().toUpperCase();
        if (dep.isEmpty() || arr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter departure and arrival airport codes.");
            return;
        }
        Date date = parseDate(dateField.getText().trim());
        if (date == null) {
            JOptionPane.showMessageDialog(this, "Invalid date. Use YYYY-MM-DD.");
            return;
        }
        boolean flexible = flexibleCheck.isSelected();

        try {
            originalResults = service.searchFlights(dep, arr, date, flexible);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "DB error: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        rawResults = new ArrayList<>(originalResults);

        // Rebuild airline filter dropdown
        allAirlines.clear();
        allAirlines.add("All");
        for (Object[] r : originalResults) {
            String airline = (String) r[3]; // airlineName
            if (!allAirlines.contains(airline)) allAirlines.add(airline);
        }
        airlineFilterBox.removeAllItems();
        for (String a : allAirlines) airlineFilterBox.addItem(a);

        applyFiltersAndSort();

        if (rawResults.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No flights found for " + dep + " → " + arr + " on " + date +
                (flexible ? " (±3 days)" : "") + ".\nCheck airport codes and try again.",
                "No Results", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void applyFiltersAndSort() {
        String selectedAirline = (String) airlineFilterBox.getSelectedItem();
        double maxPrice = Double.MAX_VALUE;
        String mp = maxPriceField.getText().trim();
        if (!mp.isEmpty()) {
            try { maxPrice = Double.parseDouble(mp); }
            catch (NumberFormatException ignored) {}
        }
        int maxStops = Integer.MAX_VALUE;
        String ms = maxStopsField.getText().trim();
        if (!ms.isEmpty()) {
            try { maxStops = Integer.parseInt(ms); }
            catch (NumberFormatException ignored) {}
        }
        java.time.LocalTime depAfter  = parseTimeFilter(depAfterField.getText());
        java.time.LocalTime arrBefore = parseTimeFilter(arrBeforeField.getText());

        List<Object[]> filtered = new ArrayList<>();
        for (Object[] r : originalResults) {
            String airlineName = (String) r[3];
            double ecoPrice    = (Double)  r[12];
            int    stops       = (Integer) r[17];
            if (selectedAirline != null && !selectedAirline.equals("All")
                    && !selectedAirline.equals(airlineName)) continue;
            if (ecoPrice > maxPrice) continue;
            if (stops > maxStops) continue;
            if (depAfter != null) {
                java.time.LocalTime dep = ((java.sql.Time) r[8]).toLocalTime();
                if (dep.isBefore(depAfter)) continue;
            }
            if (arrBefore != null) {
                java.time.LocalTime arr = ((java.sql.Time) r[9]).toLocalTime();
                if (arr.isAfter(arrBefore)) continue;
            }
            filtered.add(r);
        }

        // Sort
        String sortChoice = (String) sortBox.getSelectedItem();
        if (sortChoice != null) {
            if (sortChoice.startsWith("Departure")) {
                filtered.sort(Comparator.comparing(r -> r[8].toString()));
            } else if (sortChoice.startsWith("Arrival")) {
                filtered.sort(Comparator.comparing(r -> r[9].toString()));
            } else if (sortChoice.equals("Economy Price ↑")) {
                filtered.sort(Comparator.comparingDouble(r -> (Double) ((Object[]) r)[12]));
            } else if (sortChoice.equals("Economy Price ↓")) {
                filtered.sort((a, b) -> Double.compare((Double) b[12], (Double) a[12]));
            } else if (sortChoice.equals("Duration ↑")) {
                filtered.sort(Comparator.comparingLong(r -> durationMinutes((Object[]) r)));
            } else if (sortChoice.equals("Duration ↓")) {
                filtered.sort((a, b) -> Long.compare(durationMinutes(b), durationMinutes(a)));
            }
        }

        // Rebuild table; keep rawResults in sync with displayed rows for row-picking
        tableModel.setRowCount(0);
        rawResults = filtered;

        for (Object[] r : filtered) {
            int capacity  = (Integer) r[15];
            int booked    = (Integer) r[16];
            int available = capacity - booked;
            tableModel.addRow(new Object[]{
                r[0],   // flightId
                r[1],   // flightNum
                r[3],   // airlineName
                r[4],   // depAirportId
                r[5],   // arrAirportId
                r[8],   // depTime
                r[9],   // arrTime
                r[10],  // type
                r[11],  // daysOfWeek
                r[17],  // stops
                String.format("$%.0f", r[12]),
                String.format("$%.0f", r[13]),
                String.format("$%.0f", r[14]),
                available <= 0 ? "FULL" : available + " seats"
            });
        }
        countLabel.setText("  " + filtered.size() + " flight(s) found");
    }

    // ── Booking ───────────────────────────────────────────────────────────────

    private void openBookingDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a flight from the list first.");
            return;
        }
        Object[] flight = rawResults.get(row);

        String tripType = (String) tripTypeBox.getSelectedItem();
        boolean isRoundTrip = "Round-Trip".equals(tripType);

        // For flexible searches, ask user to confirm which exact date they want
        Date depDate;
        if (flexibleCheck.isSelected()) {
            String dateStr = JOptionPane.showInputDialog(this,
                "You searched with flexible dates.\nEnter the exact departure date (YYYY-MM-DD):",
                dateField.getText().trim());
            if (dateStr == null) return;
            depDate = parseDate(dateStr.trim());
            if (depDate == null) {
                JOptionPane.showMessageDialog(this, "Invalid date. Use YYYY-MM-DD.");
                return;
            }
        } else {
            depDate = parseDate(dateField.getText().trim());
            if (depDate == null) {
                JOptionPane.showMessageDialog(this, "Enter a valid departure date above first.");
                return;
            }
        }

        Date returnDate = null;
        if (isRoundTrip) {
            returnDate = parseDate(returnDateField.getText().trim());
            if (returnDate == null) {
                JOptionPane.showMessageDialog(this, "Enter a valid return date for round-trip.");
                return;
            }
            if (!returnDate.after(depDate)) {
                JOptionPane.showMessageDialog(this, "Return date must be after departure date.");
                return;
            }
        }

        BookingDialog dialog = new BookingDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this),
            service, customer, flight, depDate, tripType, returnDate
        );
        dialog.setVisible(true);
    }

    private static Date parseDate(String s) {
        try { return Date.valueOf(s); }
        catch (IllegalArgumentException e) { return null; }
    }

    private long durationMinutes(Object[] r) {
        java.time.LocalTime dep = ((java.sql.Time) r[8]).toLocalTime();
        java.time.LocalTime arr = ((java.sql.Time) r[9]).toLocalTime();
        long secs = dep.until(arr, java.time.temporal.ChronoUnit.SECONDS);
        if (secs < 0) secs += 86400; // overnight flight
        return secs / 60;
    }

    private java.time.LocalTime parseTimeFilter(String s) {
        s = s == null ? "" : s.trim();
        if (s.isEmpty()) return null;
        try { return java.time.LocalTime.parse(s); }
        catch (Exception e) { return null; }
    }
}
