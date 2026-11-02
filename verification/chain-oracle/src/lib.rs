// cargo test --manifest-path verification/chain-oracle/Cargo.toml --offline --locked
// Uses unchanged upstream Anyhow.
#[cfg(test)]
mod tests {
    use anyhow::Chain;
    use std::cell::Cell;
    use std::error::Error;
    use std::fmt;

    #[derive(Debug)]
    struct Node { label: &'static str, cause: Option<Box<Node>>, calls: Cell<usize> }
    impl Node {
        fn new(label: &'static str, cause: Option<Node>) -> Self {
            Self { label, cause: cause.map(Box::new), calls: Cell::new(0) }
        }
    }
    impl fmt::Display for Node {
        fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result { f.write_str(self.label) }
    }
    impl Error for Node {
        fn source(&self) -> Option<&(dyn Error + 'static)> {
            self.calls.set(self.calls.get() + 1);
            self.cause.as_ref().map(|c| &**c as &dyn Error)
        }
    }
    fn head() -> Node { Node::new("head", Some(Node::new("middle", Some(Node::new("tail", None))))) }
    #[test]
    fn linked_exhaustion_is_stable_none() {
        let node = Node::new("only", None);
        let mut chain = Chain::new(&node);
        assert_eq!(chain.next().unwrap().to_string(), "only");
        assert!(chain.next().is_none());
        assert!(chain.next().is_none());
        assert_eq!(node.calls.get(), 1);
    }
    #[test]
    fn buffered_and_default_exhaustion_are_stable_none() {
        let node = Node::new("only", None);
        let mut chain = Chain::new(&node);
        assert_eq!(chain.next_back().unwrap().to_string(), "only");
        assert!(chain.next().is_none());
        assert!(chain.next_back().is_none());
        assert!(Chain::default().next().is_none());
        assert_eq!(node.calls.get(), 1);
    }
    #[test]
    fn mixed_ends_and_size_hints_match() {
        let node = head();
        let mut chain = Chain::new(&node);
        assert_eq!(chain.size_hint(), (3, Some(3)));
        assert_eq!(chain.next().unwrap().to_string(), "head");
        assert_eq!(chain.next_back().unwrap().to_string(), "tail");
        assert_eq!(chain.size_hint(), (1, Some(1)));
        assert_eq!(chain.next().unwrap().to_string(), "middle");
        assert!(chain.next().is_none());
        assert_eq!(chain.len(), 0);
    }
    #[test]
    fn clones_share_errors_but_have_independent_iterator_state() {
        let node = head();
        let mut chain = Chain::new(&node);
        let mut clone = chain.clone();
        assert!(std::ptr::eq(chain.next().unwrap(), clone.next().unwrap()));
        assert_eq!(chain.next_back().unwrap().to_string(), "tail");
        let mut buffered_clone = chain.clone();
        assert!(std::ptr::eq(chain.next().unwrap(), buffered_clone.next().unwrap()));
        assert!(chain.next().is_none());
        assert_eq!(clone.next().unwrap().to_string(), "middle");
    }
    #[test]
    fn next_does_not_precompute_the_remaining_chain() {
        let node = head();
        let mut chain = Chain::new(&node);
        assert_eq!(node.calls.get(), 0);
        chain.next().unwrap();
        assert_eq!(node.calls.get(), 1);
        assert_eq!(node.cause.as_ref().unwrap().calls.get(), 0);
    }

    #[derive(Debug)]
    struct FailingSource(Cell<bool>);
    impl fmt::Display for FailingSource {
        fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result { f.write_str("failing") }
    }
    impl Error for FailingSource {
        fn source(&self) -> Option<&(dyn Error + 'static)> {
            if self.0.replace(false) { panic!("source failure") }
            None
        }
    }
    #[test]
    fn source_failure_does_not_lose_the_current_item() {
        let node = FailingSource(Cell::new(true));
        let mut chain = Chain::new(&node);
        let outcome = std::panic::catch_unwind(std::panic::AssertUnwindSafe(|| chain.next()));
        assert!(outcome.is_err());
        assert_eq!(chain.next().unwrap().to_string(), "failing");
        assert!(chain.next().is_none());
    }
}
