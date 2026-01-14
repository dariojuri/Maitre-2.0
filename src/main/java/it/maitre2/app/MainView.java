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
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;


import java.util.ArrayList;
import java.util.List;

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

    private final CheckBox cbSaveCsv = new CheckBox("Save result to CSV");
    private final Button btnRR = new Button("Run Round Robin");
    private final Button btnBF = new Button("Run Best First");
    private final Button btnBoth = new Button("Run Both");

    // Waiters table
    private final TableView<WaiterRow> waiterTable = new TableView<>();
    private final ObservableList<WaiterRow> waiterRows = FXCollections.observableArrayList();
    private final Button btnAddWaiter = new Button("+ Add Waiter");
    private final Button btnRemoveWaiter = new Button("- Remove Waiter");

    // Results
    private final Label lblStrategy = new Label("-");
    private final Label lblAvgWait = new Label("-");
    private final Label lblUtilCV = new Label("-");
    private final Label lblAssigned = new Label("-");
    private final Label lblCsvPath = new Label("-");

    private final TableView<CompareRow> compareTable = new TableView<>();
    private final ObservableList<CompareRow> compareRows = FXCollections.observableArrayList();

    private final TableView<WaiterResultRow> waiterResultTable = new TableView<>();
    private final ObservableList<WaiterResultRow> waiterResultRows = FXCollections.observableArrayList();

    private final CategoryAxis waiterXAxis = new CategoryAxis();
    private final NumberAxis waiterYAxis = new NumberAxis();
    private final BarChart<String, Number> waiterCompareChart = new BarChart<>(waiterXAxis, waiterYAxis);

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
                        new VBox(8,
                                waiterTable,
                                new HBox(10, btnAddWaiter, btnRemoveWaiter))

                ),
                new HBox(10, btnRR, btnBF, btnBoth, cbSaveCsv)
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
                                row("CSV", lblCsvPath)
                        )
                ),
                titled("Comparison (RR vs BF)", compareTable),
                titled("Waiters comparison (RR vs BF)", waiterCompareChart),
                titled("Waiters", waiterResultTable)
        );
        results.setPadding(new Insets(10));

        SplitPane sp = new SplitPane(config, results);
        sp.setDividerPositions(0.47);

        // setup tables
        setupWaiterTable();
        setupWaiterResultTable();
        setupCompareTable();
        setupWaiterCompareChart();

        return sp;
    }

    private void wire() {
        btnRR.setOnAction(e -> run(new RoundRobinStrategy()));
        btnBF.setOnAction(e -> run(new BestFirstStrategy()));
        btnAddWaiter.setOnAction(e -> addWaiterRow());
        btnRemoveWaiter.setOnAction(e -> removeSelectedWaiter());
        btnBoth.setOnAction(e -> runBoth());
    }

    //region Bottoni Run

    private void run(Strategy strategy) {
        try {
            SimulationConfig cfg = buildConfigFromUI();
            boolean saveLog = cbSaveCsv.isSelected();

            clearComparisonUI();

            RunResult r = SimulationRunner.run(cfg, strategy, saveLog);
            showResult(r);
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void runBoth(){
        try{
            SimulationConfig cfg = buildConfigFromUI();
            boolean saveCsv = cbSaveCsv.isSelected();

            RunResult rr = SimulationRunner.run(cfg, new RoundRobinStrategy(), saveCsv);
            RunResult bf = SimulationRunner.run(cfg, new BestFirstStrategy(), saveCsv);

            showComparison(rr, bf);
            showWaiterComparison(rr,bf);

        }catch(Exception ex){
            showError(ex.getMessage());
        }
    }

    //endregion

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

    //region Mostra Risultati

    private void showResult(RunResult r) {
        lblStrategy.setText(r.strategyName);
        lblAssigned.setText(String.valueOf(r.assignedTasks));
        lblAvgWait.setText(String.format("%.4f", r.avgTaskWait));
        lblUtilCV.setText(String.format("%.4f", r.utilizationCV));
        lblCsvPath.setText(r.logFilePath == null ? "-" : "result.csv");

        waiterResultRows.clear();
        for (WaiterSnapshot w : r.waiters) {
            waiterResultRows.add(new WaiterResultRow(
                    w.id, w.efficiency, w.workloadMinutes, w.utilization
            ));
        }
    }

    private void showComparison(RunResult rr, RunResult bf){
        compareRows.clear();

        compareRows.add(new CompareRow("Assigned tasks",
                String.valueOf(rr.assignedTasks),
                String.valueOf(bf.assignedTasks)));

        compareRows.add(new CompareRow("Avg task wait",
                String.format("%.3f", rr.avgTaskWait),
                String.format("%.3f", bf.avgTaskWait)));

        compareRows.add(new CompareRow("Utilization CV",
                String.format("%.3f", rr.utilizationCV),
                String.format("%.3f", bf.utilizationCV)));

        //Done Tables
        int rrDone = rr.tablesByState.getOrDefault(TableState.DONE, 0);
        int bfDone = bf.tablesByState.getOrDefault(TableState.DONE, 0);

        compareRows.add(new CompareRow("Tables DONE",
                String.valueOf(rrDone),
                String.valueOf(bfDone)));
    }

    private void showWaiterComparison(RunResult rr, RunResult bf){
        waiterCompareChart.getData().clear();

        XYChart.Series<String, Number> sRR = new XYChart.Series<>();
        sRR.setName("RoundRobin");

        XYChart.Series<String, Number> sBF = new XYChart.Series<>();
        sBF.setName("BF");

        //assumiamo stessi ID in entrambi
        for(WaiterSnapshot w : rr.waiters){
            sRR.getData().add(new XYChart.Data<>(String.valueOf(w.id), w.utilization));
        }
        for(WaiterSnapshot w : bf.waiters){
            sBF.getData().add(new XYChart.Data<>(String.valueOf(w.id), w.utilization));
        }

        waiterCompareChart.getData().addAll(sRR, sBF);
    }

    //endregion

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
        waiterTable.setPrefHeight(200);
    }

    private void setupCompareTable(){
        compareTable.setItems(compareRows);

        TableColumn<CompareRow, String> colMetric = new TableColumn<>("Metric");
        colMetric.setMinWidth(200);
        colMetric.setCellValueFactory(c -> c.getValue().metric);


        TableColumn<CompareRow, String> colRR = new TableColumn<>("RoundRobin");
        colRR.setMinWidth(100);
        colRR.setCellValueFactory(c -> c.getValue().rr);

        TableColumn<CompareRow, String> colBF = new TableColumn<>("BestFirst");
        colBF.setMinWidth(100);
        colBF.setCellValueFactory(c -> c.getValue().bf);

        compareTable.getColumns().addAll(colMetric, colRR, colBF);
        compareTable.setPrefHeight(290);
    }

    //region Waiter

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

    private void addWaiterRow(){
        int nextId = waiterRows.isEmpty() ? 1 : waiterRows.get(waiterRows.size() - 1).id.get() + 1;
        waiterRows.add(new WaiterRow(nextId, 1.0));
    }

    private void removeSelectedWaiter(){
        WaiterRow sel = waiterTable.getSelectionModel().getSelectedItem();
        if(sel == null) return;
        if(waiterRows.size() <= 1){
            showError("You must have at least 1 waiter.");
            return;
        }
        waiterRows.remove(sel);
    }

    private void setupWaiterCompareChart(){
        waiterCompareChart.setTitle("Waiters utilizazion comparison");
        waiterXAxis.setLabel("Waiter ID");
        waiterYAxis.setLabel("Utilization");
        waiterCompareChart.setLegendVisible(true);
        waiterCompareChart.setAnimated(false);
        waiterCompareChart.setCategoryGap(10);
        waiterCompareChart.setBarGap(3);
        waiterCompareChart.setPrefHeight(260);
    }
    //endregion


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

    private void clearComparisonUI(){
        compareRows.clear();                    //Tabella confronto
        waiterCompareChart.getData().clear();   //Grafico confronto
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

    public static class CompareRow {
        public final StringProperty metric = new SimpleStringProperty();
        public final StringProperty rr = new SimpleStringProperty();
        public final StringProperty bf = new SimpleStringProperty();

        public CompareRow(String metric, String rr, String bf) {
            this.metric.set(metric);
            this.rr.set(rr);
            this.bf.set(bf);
        }
    }


}
