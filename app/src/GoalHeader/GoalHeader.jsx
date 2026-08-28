import styles from './GoalHeader.module.css';

function GoalHeader(){
    return(
        <div className={styles.header}>
            Goal
            <div className={styles.filter}>
                Filter
                <span className="material-symbols-outlined">keyboard_arrow_down</span>
            </div>
        </div>
    );
}

export default GoalHeader