import React from 'react';
import { AlertTriangle, PhoneCall } from 'lucide-react';

export default function EmergencyBanner() {
  return (
    <div className="emergency-banner" role="alert" aria-label="Official Cyber Emergency Guidance">
      <div className="container" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
        <AlertTriangle size={16} />
        <span>
          <strong>URGENT NOTICE:</strong> CyberSaarthi is an academic educational portal. For real-time financial cyber fraud, immediately dial 
          <a href="tel:1930" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.25rem' }}>
            <PhoneCall size={14} /> 1930
          </a> or report to the official National Cyber Crime Reporting Portal:
          <a href="https://cybercrime.gov.in" target="_blank" rel="noopener noreferrer">
            cybercrime.gov.in
          </a>
        </span>
      </div>
    </div>
  );
}
