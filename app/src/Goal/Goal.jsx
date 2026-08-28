import { useEffect, useState } from 'react';
import axios from 'axios';
import styles from './Goal.module.css'
import GoalHeader from '../GoalHeader/GoalHeader';

const CARD_COLORS = ['#f5d6d6', '#f4f0c9'];

function Goal(){
    const [goals, setGoals] = useState([]);
    const [error, setError] = useState(null);

    useEffect(() => {
        axios.get('/goals')
            .then(res => setGoals(res.data))
            .catch(err => setError(err.message));
    }, []);

    return(
        <div className={styles.parent}>
            <GoalHeader/>
            <div className={styles.list}>
                {error && <div className={styles.error}>Couldn't load goals: {error}</div>}
                {!error && goals.length === 0 && <div className={styles.empty}>No goals yet.</div>}
                {goals.map((goal, i) => (
                    <div
                        key={goal.id}
                        className={styles.card}
                        style={{ backgroundColor: CARD_COLORS[i % CARD_COLORS.length] }}
                    >
                        <input type="checkbox" checked={goal.completed} readOnly className={styles.checkbox} />
                        <div className={styles.cardBody}>
                            <div className={styles.cardTop}>
                                <span className={styles.cardName}>{goal.name}</span>
                                {goal.finishByDate && <span className={styles.cardDate}>{goal.finishByDate}</span>}
                            </div>
                            <p className={styles.cardDesc}>{goal.description}</p>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default Goal