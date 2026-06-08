import React from 'react';
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Stepper } from '../Stepper';

const STEPS = ['Tipo', 'Mascota', 'Contacto'];

describe('Stepper', () => {
  it('renderiza todas las etiquetas de los pasos', () => {
    render(<Stepper steps={STEPS} current={0} />);
    STEPS.forEach(label => {
      expect(screen.getByText(label)).toBeInTheDocument();
    });
  });

  it('el paso activo muestra el número correcto', () => {
    render(<Stepper steps={STEPS} current={1} />);
    // step index 1 → shows "2" (i + 1)
    expect(screen.getByText('2')).toBeInTheDocument();
  });

  it('los pasos futuros muestran su número', () => {
    render(<Stepper steps={STEPS} current={0} />);
    // index 1 and 2 are future → show "2" and "3"
    expect(screen.getByText('2')).toBeInTheDocument();
    expect(screen.getByText('3')).toBeInTheDocument();
  });

  it('acepta accent="green" sin lanzar errores', () => {
    const { container } = render(<Stepper steps={STEPS} current={1} accent="green" />);
    expect(container.firstChild).not.toBeNull();
  });
});
