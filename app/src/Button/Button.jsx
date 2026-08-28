import styles from './Button.module.css';

function Button() {

    return (
        <div style={{display: 'flex'}}>
            <button className={`${styles.btn}`}>
                Home
            </button>

            <button className={`${styles.btn} ${styles.btn2}`}>
                Goal
            </button>

            <button className={`${styles.btn}`}>
                Schedule
            </button>

            <button className={`${styles.btn}  ${styles.btn3}`}>
                Routine
            </button>
        </div>
    );
}

export default Button