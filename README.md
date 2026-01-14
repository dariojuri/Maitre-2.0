# Maitre 2.0

Maitre 2.0 è una simulazione event-driven del servizio di sala in un ristorante.
Il progetto confronta due strategie di assegnazione dei task ai camerieri:
- **Round Robin** (baseline)
- **Best-First** (euristica su coppie task–cameriere)

## Requisiti
- Java (consigliato 17+)
- JavaFX SDK configurato in IntelliJ (module-path + add-modules)

## Esecuzione
Avvia la GUI eseguendo la main class:

`it.maitre2.app.AppMain`

Dalla GUI puoi:
- impostare i parametri (durata, seed, arrivi, cucina)
- modificare efficienze dei camerieri (tabella)
- eseguire **Run Round Robin**, **Run Best First** o **Run Both**
- esportare risultati su CSV con **Save results to CSV**

## Output CSV
Se l’opzione **Save results to CSV** è attiva, viene aggiornato il file:
- `results.csv`

Ogni riga corrisponde a una simulazione (strategia + seed + parametri) e include:
- avgTaskWait
- utilizationCV
- doneTables
- makespan

## Struttura del progetto (alto livello)
- `simulation/` motore e gestione eventi
- `model/` entità (Table, Waiter, Task, ...)
- `agent/` strategie (RoundRobin, BestFirst)
- `metrics/` metriche e raccolta risultati
- `app/` GUI e runner (bridge tra GUI e core)

## Note
Il confronto tra strategie avviene su scenari ripetibili grazie all’utilizzo di un seed.
