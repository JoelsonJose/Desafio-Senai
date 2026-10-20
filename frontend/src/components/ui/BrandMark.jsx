export default function BrandMark({ size = 70, className = 'rounded-[20px]' }) {
  return (
    <div className={`flex shrink-0 items-center justify-center bg-brand-600 ${className}`} style={{ width: size, height: size }} aria-hidden="true">
      <svg viewBox="0 0 40 48" width={Math.round(size * 0.54)} height={Math.round(size * 0.6)} aria-hidden="true">
        <path d="M7 5v38" fill="none" stroke="white" strokeWidth="6" strokeLinecap="round" />
        <path d="M8 22c0-8 5-13 13-13 8 0 13 5 13 13s-5 13-13 13c-5 0-9-2-13-6" fill="none" stroke="white" strokeWidth="6" strokeLinecap="round" strokeLinejoin="round" />
        <circle cx="21" cy="22" r="2.4" fill="white" />
      </svg>
    </div>
  );
}
