import Button from './Button/Button.jsx'
import Calendar from './Calendar/Calendar.jsx'
import Goal from './Goal/Goal.jsx'

function App() {

  return (
    <div style={{ position: 'relative', display: 'flex', flexDirection: 'column', height: '100vh' }}>
      <Button />
      <div style={{ display: 'flex', flex: 1, gap: '2rem', padding: 'clamp(0.2rem, 1.5vw + 0.2rem, 2rem)', minHeight: 0 }}>
        <Calendar />
        <Goal />
      </div>
    </div>

  );
}

export default App;
