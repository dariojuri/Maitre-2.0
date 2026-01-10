package it.maitre2.model;

public enum TableState {
    NEW,            //nuovo tavolo appena arrivato
    WAITING_ORDER,  //pronto ad ordinare
    WAITING_FOOD,   //ha ordinato, attende piatti
    WAITING_BILL,   //chiede il conto
    DONE            //tavolo chiuso
}
