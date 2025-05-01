import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';

// A basic passing test component
describe('Basic Test Suite', () => {
  test('renders a simple component', () => {
    // Render a simple div with test text
    render(<div data-testid="test-element">Test Content</div>);
    
    // Assert the component rendered correctly
    expect(screen.getByTestId('test-element')).toBeInTheDocument();
    expect(screen.getByText('Test Content')).toBeInTheDocument();
  });
});
