export function controlClassName(hasError, paddingClass = 'px-3') {
  const border = hasError
    ? 'border-red-500 focus:ring-red-500/20'
    : 'border-slate-300 focus:border-brand-600 focus:ring-brand-600/20';
  return `h-10 rounded-lg border bg-white text-sm text-slate-900 outline-none transition focus:ring-2 disabled:cursor-not-allowed disabled:bg-slate-50 disabled:text-slate-400 ${paddingClass} ${border}`;
}

export default function Field({ label, htmlFor, error, hint, children }) {
  return (
    <div>
      <label htmlFor={htmlFor} className="mb-1 block text-sm font-medium text-slate-800">
        {label}
      </label>
      {children}
      {error ? (
        <p className="mt-1 text-xs text-red-600" role="alert">
          {error}
        </p>
      ) : (
        hint && <p className="mt-1 text-xs text-slate-500">{hint}</p>
      )}
    </div>
  );
}

export function TextField({ label, name, value, onChange, error, hint, ...inputProps }) {
  return (
    <Field label={label} htmlFor={name} error={error} hint={hint}>
      <input
        id={name}
        name={name}
        value={value}
        onChange={(event) => onChange(name, event.target.value)}
        aria-invalid={Boolean(error)}
        className={`${controlClassName(Boolean(error))} w-full`}
        {...inputProps}
      />
    </Field>
  );
}

export function SelectField({ label, name, value, onChange, options, placeholder, error, hint, ...selectProps }) {
  return (
    <Field label={label} htmlFor={name} error={error} hint={hint}>
      <select
        id={name}
        name={name}
        value={value}
        onChange={(event) => onChange(name, event.target.value)}
        aria-invalid={Boolean(error)}
        className={`${controlClassName(Boolean(error))} w-full`}
        {...selectProps}
      >
        {placeholder && <option value="">{placeholder}</option>}
        {options.map((option) => (
          <option key={option.value} value={option.value} disabled={option.disabled}>
            {option.label}
          </option>
        ))}
      </select>
    </Field>
  );
}
