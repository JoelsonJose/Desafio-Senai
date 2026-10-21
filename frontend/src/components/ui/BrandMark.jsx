export default function BrandMark({ size = 70, className = 'rounded-[20px]' }) {
  return (
    <div className={`flex shrink-0 items-center justify-center bg-brand-600 ${className}`} style={{ width: size, height: size }} aria-hidden="true">
      <svg viewBox="0 0 40 48" width={Math.round(size * 0.54)} height={Math.round(size * 0.6)} aria-hidden="true">
        <path d="M7 5v38" fill="none" stroke="white" strokeWidth="8" strokeLinecap="round" />
        <path d="M8 16 C8 9.9 12.9 5 19 5 C25.1 5 30 9.9 30 16 C30 22.1 25.1 27 19 27 C15.5 27 8.5 24.5 8 17" fill="none" stroke="white" strokeWidth="8" strokeLinecap="round" strokeLinejoin="round" />
        <circle cx="19" cy="16" r="2.4" fill="white" />
      </svg>
    </div>
  );
}