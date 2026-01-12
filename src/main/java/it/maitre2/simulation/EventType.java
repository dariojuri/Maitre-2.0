package it.maitre2.simulation;

public enum EventType {
    ARRIVAL,            //genera un nuovo tavolo e schedula il prossimo
    NEW_TABLE,          //arriva un nuovo tavolo (creazione/attivazione)
    READY_TO_ORDER,     //il tavolo è pronto ad ordinare -> genera Task TAKE_ORDER
    FOOD_READY,         //piatti pronti -> genera Task SERVE_FOOD
    BILL_REQUEST,       //richiesta conto -> genera Task BILL
    TASK_DONE
}
