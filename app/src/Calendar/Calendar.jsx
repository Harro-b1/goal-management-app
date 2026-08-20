import styles from './Calendar.module.css';
import CalendarHeader from '../CalendarHeader/CalendarHeader.jsx';

function Calendar() {
    return (
        <div className={styles.parent}>
            <CalendarHeader/>
            <div className={styles.cal}>
            </div>
        </div>
    );
}

export default Calendar