import React from 'react';
import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { Button } from '../Button';


describe('Button', () => {
  it('renderiza el texto del children', () => {
    render(<Button>Guardar</Button>);
    expect(screen.getByRole('button', { name: 'Guardar' })).toBeInTheDocument();
  });

  it('type="button" por defecto (no envía el form accidentalmente)', () => {
    render(<Button>OK</Button>);
    expect(screen.getByRole('button')).toHaveAttribute('type', 'button');
  });

  it('type="submit" cuando se pasa la prop', () => {
    render(<Button type="submit">Enviar</Button>);
    expect(screen.getByRole('button')).toHaveAttribute('type', 'submit');
  });

  it('llama a onClick al hacer clic', () => {
    const handleClick = vi.fn();
    render(<Button onClick={handleClick}>Clic</Button>);
    fireEvent.click(screen.getByRole('button'));
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  it('deshabilitado: no llama a onClick y tiene atributo disabled', () => {
    const handleClick = vi.fn();
    render(<Button disabled onClick={handleClick}>Bloqueado</Button>);
    const btn = screen.getByRole('button');
    fireEvent.click(btn);
    expect(handleClick).not.toHaveBeenCalled();
    expect(btn).toBeDisabled();
  });
});
