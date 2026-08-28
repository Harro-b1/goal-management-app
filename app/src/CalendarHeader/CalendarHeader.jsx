import styles from './CalendarHeader.module.css';

function CalendarHeader(){
    return(
        <div className={styles.header}>
            CalendarHeader
            <div className={styles.event}>
                <span className="material-symbols-outlined">add</span>
                new event
            </div>
        </div>
    );
}

export default CalendarHeader