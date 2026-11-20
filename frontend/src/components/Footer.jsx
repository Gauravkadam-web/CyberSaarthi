import React from 'react';
import { Shield } from 'lucide-react';

export default function Footer() {
  return (
    <footer className="footer" role="contentinfo">
      <div className="container" style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem', textAlign: 'center' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', color: '#FFFFFF', fontWeight: 700 }}>
          <Shield size={20} />
          <span>CyberSaarthi — Your Guide to a Safer Digital World</span>
        </div>
        <p style={{ maxWidth: '800px', margin: '0 auto', color: '#94A3B8', fontSize: '0.85rem' }}>
          <strong>Academic Disclaimer:</strong> CyberSaarthi is a student project for cybercrime awareness education, structured reporting support, and research analytics. It does not provide legal counsel, dispute resolution, or official police investigation services.
        </p>
        <p style={{ color: '#64748B', fontSize: '0.8rem' }}>
          National Emergency Cyber Helpline: <strong>1930</strong> | Official Government Portal: <a href="https://cybercrime.gov.in" target="_blank" rel="noopener noreferrer" style={{ color: '#93C5FD' }}>cybercrime.gov.in</a>
        </p>
      </div>
    </footer>
  );
}
