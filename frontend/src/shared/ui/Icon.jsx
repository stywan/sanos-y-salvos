import {
  PawPrint, Bell, MapPin, Check, X, House, Camera, Phone,
  Share2, Eye, Download, Search, SlidersHorizontal, ArrowRight,
  ChevronLeft, ChevronRight, AlertCircle, Plus, LogOut, User, Menu,
} from 'lucide-react';

const makeIcon = (Component, defaultSize = 18) =>
  ({ size = defaultSize, color = 'currentColor', strokeWidth = 1.75, style, className } = {}) => (
    <Component size={size} color={color} strokeWidth={strokeWidth} style={style} className={className} />
  );

export const Icon = {
  Paw:     makeIcon(PawPrint, 18),
  Bell:    makeIcon(Bell, 20),
  MapPin:  makeIcon(MapPin, 14),
  Check:   ({ size = 14, color = 'currentColor' } = {}) => <Check size={size} color={color} strokeWidth={2.5} />,
  X:       makeIcon(X, 16),
  Home:    makeIcon(House, 15),
  Camera:  makeIcon(Camera, 28),
  Phone:   makeIcon(Phone, 15),
  Share:   makeIcon(Share2, 15),
  Eye:     makeIcon(Eye, 14),
  Download: makeIcon(Download, 14),
  Search:  makeIcon(Search, 16),
  Filter:  makeIcon(SlidersHorizontal, 14),
  Plus:    makeIcon(Plus, 16),
  User:    makeIcon(User, 16),
  LogOut:  makeIcon(LogOut, 15),
  Arrow: ({ size = 14, dir = 'right', color = 'currentColor' } = {}) => {
    const rot = { right: 0, left: 180, up: -90, down: 90 }[dir] ?? 0;
    return (
      <span style={{ display: 'inline-flex', transform: `rotate(${rot}deg)` }}>
        <ArrowRight size={size} color={color} />
      </span>
    );
  },
  ChevronLeft:  makeIcon(ChevronLeft, 16),
  ChevronRight: makeIcon(ChevronRight, 16),
  Alert: makeIcon(AlertCircle, 16),
  Menu:  makeIcon(Menu, 22),
};
