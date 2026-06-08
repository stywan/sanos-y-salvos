import React from 'react';
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Tip } from '../Tip';

describe('Tip', () => {
  it('muestra el título', () => {
    render(<Tip title="Sé rápido" body="Actúa en las primeras horas." />);
    expect(screen.getByText('Sé rápido')).toBeInTheDocument();
  });

  it('muestra el cuerpo del consejo', () => {
    render(<Tip title="Consejo" body="Publica en redes sociales." />);
    expect(screen.getByText('Publica en redes sociales.')).toBeInTheDocument();
  });

  it('muestra el badge "Consejo del Guardián"', () => {
    render(<Tip title="T" body="B" />);
    expect(screen.getByText('Consejo del Guardián')).toBeInTheDocument();
  });
});
