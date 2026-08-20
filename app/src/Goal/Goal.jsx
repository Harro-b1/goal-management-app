import styles from './Goal.module.css'
import GoalHeader from '../GoalHeader/GoalHeader';

function Goal(){
    return(
        <div className={styles.parent}>
            <GoalHeader/>
            <div className={styles.goal}></div>
        </div>
    );
}

export default Goal