package it.maitre2.app;

import it.maitre2.agent.BestFirstStrategy;
import it.maitre2.agent.RoundRobinStrategy;
import it.maitre2.agent.Strategy;
import it.maitre2.model.TableState;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.util.converter.DoubleStringConverter;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class MainView {

    // --- UI state ---
    private final TextField tfDuration = new TextField("240");
    private final TextField tfSeed = new TextField("42");
    private final TextField tfMaxTables = new TextField("60");

    private final TextField tfMinIA = new TextField("1.0");
    private final TextField tfMaxIA = new TextField("8.0");
    private final TextField tfK = new TextField("10.0");

    private final TextField tfKitchenBase = new TextField("5.0");
    private final TextField tfKitchenPerPlate = new TextField("1.0");
    private final TextField tfKitchenJitter = new TextField("3.0");

    private final CheckBox cbSaveLog = new CheckBox("Save log to file");
    private final Button btnRR = new Button("Run Round Robin");
    private final Button btnBF = new Button("Run Best First");

    // Waiters table
    private final TableView<WaiterRow> waiterTable = new TableView<>();
    private final ObservableList<WaiterRow> waiterRows = FXCollections.observableArrayList();

    // Results
    private final Label lblStrategy = new Label("-");
    private final Label lblAvgWait = new Label("-");
    private final Label lblUtilCV = new Label("-");
    private final Label lblAssigned = new Label("-");
    private final Label lblLogPath = new Label("-");

    private final Map<TableState, Label> stateLabels = new EnumMap<>(TableState.class);

    private final TableView<WaiterResultRow> waiterResultTable = new TableView<>();
    private final ObservableList<WaiterResultRow> waiterResultRows = FXCollections.observableArrayList();

    private final Parent root;

    public MainView() {
        root = build();
        wire();
        initDefaultWaiters(3);
    }

    public Parent getRoot() { return root; }

    private Parent build() {
        // left: config
        VBox config = new VBox(10,
                titled("Simulation",
                        grid(
                                row("Duration (min)", tfDuration),
                                row("Seed", tfSeed),
                                row("Max tables", tfMaxTables)
                        )
                ),
                titled("Arrivals (sigmoid)",
                        grid(
                                row("Min inter-arrival", tfMinIA),
                                row("Max inter-arrival", tfMaxIA),
                                row("k (steepness)", tfK)
                        )
                ),
                titled("Kitchen",
                        grid(
                                row("Base minutes", tfKitchenBase),
                                row("Per plate", tfKitchenPerPlate),
                                row("Jitter max", tfKitchenJitter)
                        )
                ),
                titled("Waiters",
                        waiterTable
                ),
                new HBox(10, btnRR, btnBF, cbSaveLog)
        );
        config.setPadding(new Insets(10));
        config.setPrefWidth(420);

        // right: results
        VBox results = new VBox(10,
                titled("Result summary",
                        grid(
                                row("Strategy", lblStrategy),
                                row("Assigned tasks", lblAssigned),
                                row("Avg task wait", lblAvgWait),
                                row("Utilization CV", lblUtilCV),
                                row("Log file", lblLogPath)
                        )
                ),
                titled("Tables by state", buildStateBox()),
                titled("Waiters", waiterResultTable)
        );
        results.setPadding(new Insets(10));

        SplitPane sp = new SplitPane(config, results);
        sp.setDividerPositions(0.47);

        // setup tables
        setupWaiterTable();
        setupWaiterResultTable();

        return sp;
    }

    private void wire() {
        btnRR.setOnAction(e -> run(new RoundRobinStrategy()));
        btnBF.setOnAction(e -> run(new BestFirstStrategy()));
    }

    private void run(Strategy strategy) {
        try {
            SimulationConfig cfg = buildConfigFromUI();
            boolean saveLog = cbSaveLog.isSelected();
            RunResult r = SimulationRunner.run(cfg, strategy, saveLog);
            showResult(r);
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private SimulationConfig buildConfigFromUI() {
        double duration = parseDouble(tfDuration);
        long seed = parseLong(tfSeed);
        int maxTables = parseInt(tfMaxTables);

        double minIA = parseDouble(tfMinIA);
        double maxIA = parseDouble(tfMaxIA);
        double k = parseDouble(tfK);

        double kb = parseDouble(tfKitchenBase);
        double kpp = parseDouble(tfKitchenPerPlate);
        double kj = parseDouble(tfKitchenJitter);

        List<Double> effs = new ArrayList<>();
        for (WaiterRow wr : waiterRows) effs.add(wr.efficiency.get());


        return new SimulationConfig(
                duration, seed,
                effs,
                minIA, maxIA, k,
                maxTables,
                kb, kpp, kj
        );
    }

    private void showResult(RunResult r) {
        lblStrategy.setText(r.strategyName);
        lblAssigned.setText(String.valueOf(r.assignedTasks));
        lblAvgWait.setText(String.format("%.4f", r.avgTaskWait));
        lblUtilCV.setText(String.format("%.4f", r.utilizationCV));
        lblLogPath.setText(r.logFilePath == null ? "-" : r.logFilePath);

        for (TableState s : TableState.values()) {
            int v = r.tablesByState.getOrDefault(s, 0);
            stateLabels.get(s).setText(String.valueOf(v));
        }

        waiterResultRows.clear();
        for (WaiterSnapshot w : r.waiters) {
            waiterResultRows.add(new WaiterResultRow(
                    w.id, w.efficiency, w.workloadMinutes, w.utilization
            ));
        }
    }

    // ----- UI helpers -----
    private void initDefaultWaiters(int n) {
        waiterRows.clear();
        for (int i = 1; i <= n; i++) waiterRows.add(new WaiterRow(i, 1.0));
    }

    private void setupWaiterTable() {
        waiterTable.setEditable(true);
        waiterTable.setItems(waiterRows);

        TableColumn<WaiterRow, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> c.getValue().id);

        TableColumn<WaiterRow, Double> colEff = new TableColumn<>("Efficiency");
        colEff.setCellValueFactory(c -> c.getValue().efficiency.asObject());
        colEff.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colEff.setOnEditCommit(evt -> evt.getRowValue().efficiency.set(evt.getNewValue()));

        waiterTable.getColumns().addAll(colId, colEff);
        waiterTable.setPrefHeight(160);
    }

    private void setupWaiterResultTable() {
        waiterResultTable.setItems(waiterResultRows);

        TableColumn<WaiterResultRow, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> c.getValue().id);

        TableColumn<WaiterResultRow, Number> colEff = new TableColumn<>("Eff");
        colEff.setCellValueFactory(c -> c.getValue().efficiency);

        TableColumn<WaiterResultRow, Number> colWork = new TableColumn<>("Workload (min)");
        colWork.setCellValueFactory(c -> c.getValue().workload);

        TableColumn<WaiterResultRow, Number> colUtil = new TableColumn<>("Utilization");
        colUtil.setCellValueFactory(c -> c.getValue().utilization);

        waiterResultTable.getColumns().addAll(colId, colEff, colWork, colUtil);
        waiterResultTable.setPrefHeight(250);
    }

    private GridPane buildStateBox() {
        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(6);

        int r = 0;
        for (TableState s : TableState.values()) {
            Label name = new Label(s.name());
            Label val = new Label("0");
            stateLabels.put(s, val);
            g.addRow(r++, name, val);
        }
        return g;
    }

    private TitledPane titled(String title, javafx.scene.Node content) {
        TitledPane tp = new TitledPane(title, content);
        tp.setCollapsible(false);
        return tp;
    }

    private GridPane grid(javafx.scene.Node... rows) {
        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(6);
        int i = 0;
        for (javafx.scene.Node n : rows) g.add(n, 0, i++);
        return g;
    }

    private HBox row(String label, javafx.scene.Node field) {
        Label l = new Label(label);
        l.setMinWidth(150);
        HBox h = new HBox(10, l, field);
        return h;
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText("Cannot run simulation");
        a.setContentText(msg);
        a.showAndWait();
    }

    private double parseDouble(TextField tf) { return Double.parseDouble(tf.getText().trim()); }
    private int parseInt(TextField tf) { return Integer.parseInt(tf.getText().trim()); }
    private long parseLong(TextField tf) { return Long.parseLong(tf.getText().trim()); }

    // ----- table row classes -----
    public static class WaiterRow {
        public final IntegerProperty id = new SimpleIntegerProperty();
        public final DoubleProperty efficiency = new SimpleDoubleProperty();

        public WaiterRow(int id, double eff) {
            this.id.set(id);
            this.efficiency.set(eff);
        }
    }

    public static class WaiterResultRow {
        public final IntegerProperty id = new SimpleIntegerProperty();
        public final DoubleProperty efficiency = new SimpleDoubleProperty();
        public final DoubleProperty workload = new SimpleDoubleProperty();
        public final DoubleProperty utilization = new SimpleDoubleProperty();

        public WaiterResultRow(int id, double eff, double workload, double util) {
            this.id.set(id);
            this.efficiency.set(eff);
            this.workload.set(workload);
            this.utilization.set(util);
        }
    }
}
