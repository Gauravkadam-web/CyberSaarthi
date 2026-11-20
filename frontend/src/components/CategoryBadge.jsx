import React from 'react';

const CATEGORY_MAP = {
  FINANCIAL_UPI_FRAUD: { label: 'Financial & UPI', className: 'badge-financial' },
  PHISHING_SOCIAL_ENGINEERING: { label: 'Phishing Defense', className: 'badge-phishing' },
  IDENTITY_THEFT: { label: 'Identity Theft', className: 'badge-identity' },
  SOCIAL_ACCOUNT_COMPROMISE: { label: 'Social Media Security', className: 'badge-social' },
  CYBERSTALKING_HARASSMENT: { label: 'Cyberstalking', className: 'badge-danger' },
  JOB_INVESTMENT_SCAM: { label: 'Job & Investment', className: 'badge-financial' },
  MALWARE_RANSOMWARE: { label: 'Malware & Ransomware', className: 'badge-danger' },
  OTHER_UNCLASSIFIED: { label: 'General Threat', className: 'badge-default' }
};

export default function CategoryBadge({ categoryCode }) {
  const meta = CATEGORY_MAP[categoryCode] || { label: categoryCode, className: 'badge-default' };
  return (
    <span className={`badge ${meta.className}`}>
      {meta.label}
    </span>
  );
}
