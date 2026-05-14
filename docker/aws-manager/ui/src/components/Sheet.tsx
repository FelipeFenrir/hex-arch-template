import React, { ReactNode } from 'react'

interface SheetProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  children: ReactNode
}

interface SheetContentProps {
  children: ReactNode
  className?: string
}

interface SheetHeaderProps {
  children: ReactNode
  className?: string
}

interface SheetTitleProps {
  children: ReactNode
  className?: string
}

interface SheetDescriptionProps {
  children: ReactNode
  className?: string
}

export function Sheet({ open, onOpenChange, children }: SheetProps) {
  React.useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && open) {
        onOpenChange(false)
      }
    }
    if (open) {
      window.addEventListener('keydown', handleEscape)
      return () => window.removeEventListener('keydown', handleEscape)
    }
  }, [open, onOpenChange])

  if (!open) return null

  return (
    <>
      <div
        className="fixed inset-0 z-40 bg-black/50"
        onClick={() => onOpenChange(false)}
      />
      <div className="fixed right-0 top-0 z-50 h-screen w-full max-w-lg shadow-lg">
        {children}
      </div>
    </>
  )
}

export function SheetContent({ children, className = '' }: SheetContentProps) {
  return (
    <div className={`flex h-full flex-col bg-white ${className}`}>
      {children}
    </div>
  )
}

export function SheetHeader({ children, className = '' }: SheetHeaderProps) {
  return (
    <div className={`border-b px-6 py-4 ${className}`}>
      {children}
    </div>
  )
}

export function SheetTitle({ children, className = '' }: SheetTitleProps) {
  return (
    <h2 className={`text-lg font-semibold ${className}`}>
      {children}
    </h2>
  )
}

export function SheetDescription({ children, className = '' }: SheetDescriptionProps) {
  return (
    <p className={`text-sm text-gray-600 ${className}`}>
      {children}
    </p>
  )
}

