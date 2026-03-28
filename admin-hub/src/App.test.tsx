import { render, screen } from '@testing-library/react'
import App from './App'

describe('App', () => {
  it('renders the hello world hero copy', () => {
    render(<App />)

    expect(screen.getByRole('heading', { name: 'Hello, World.' })).toBeInTheDocument()
    expect(screen.getByText(/Admin Hub is online/i)).toBeInTheDocument()
  })

  it('shows the configured frontend status panel', () => {
    render(<App />)

    expect(screen.getByText('ReactJS + webpack')).toBeInTheDocument()
    expect(screen.getByText('3000')).toBeInTheDocument()
  })
})
