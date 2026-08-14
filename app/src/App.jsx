import Button from './Button/Button.jsx'
import Calendar from './Calendar/Calendar.jsx'
import Goal from './Goal/Goal.jsx'

function App() {

  return (
    <div style={{position: 'relative'}}>
      <Button/>
      <div style={{ display: 'flex', height: '100vh', gap: '1rem' }}>
  <Calendar/>
  <Goal/>
</div>
    </div>

  );
}

export default App;
