import React from 'react';
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Badge } from '../Badge';

describe('Badge', () => {
  it('PERDIDO muestra la etiqueta correcta', () => {
    render(<Badge type="PERDIDO" />);
    expect(screen.getByText('PERDIDO')).toBeInTheDocument();
  });

  it('ENCONTRADO muestra la etiqueta correcta', () => {
    render(<Badge type="ENCONTRADO" />);
    expect(screen.getByText('ENCONTRADO')).toBeInTheDocument();
  });

  it('REUNIDO muestra la etiqueta correcta', () => {
    render(<Badge type="REUNIDO" />);
    expect(screen.getByText('REUNIDO')).toBeInTheDocument();
  });

  it('tipo desconocido no renderiza nada', () => {
    const { container } = render(<Badge type="INVALIDO" />);
    expect(container.firstChild).toBeNull();
  });

  it('sin type no renderiza nada', () => {
    const { container } = render(<Badge />);
    expect(container.firstChild).toBeNull();
  });
});
